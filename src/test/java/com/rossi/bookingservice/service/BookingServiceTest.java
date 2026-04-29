package com.rossi.bookingservice.service;

import com.rossi.bookingservice.config.RabbitMQConfig;
import com.rossi.bookingservice.dto.PagamentoMessageDTO;
import com.rossi.bookingservice.dto.request.ReservaRequestDTO;
import com.rossi.bookingservice.dto.response.ReservaResponseDTO;
import com.rossi.bookingservice.exception.RegraNegocioException;
import com.rossi.bookingservice.model.Assento;
import com.rossi.bookingservice.model.enums.Metodo;
import com.rossi.bookingservice.model.enums.TipoAssento;
import com.rossi.bookingservice.repository.AssentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService — Testes Unitários")
class BookingServiceTest {

    @Mock
    private AssentoRepository assentoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingService bookingService;

    private Assento assentoLivre;

    @BeforeEach
    void setUp() {
        assentoLivre = new Assento();
        assentoLivre.setId(1L);
        assentoLivre.setFileira("A");
        assentoLivre.setNumero(5);
        assentoLivre.setTipo(TipoAssento.VIP);
        assentoLivre.setValor(new BigDecimal("50.00"));
        assentoLivre.setOcupado(false);
    }

    // -----------------------------------------------------------------------
    // Cenário 1: Fluxo feliz — reserva bem-sucedida
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Deve reservar assento com sucesso, marcar como ocupado e publicar mensagem")
    void deveReservarAssentoComSucesso() {
        ReservaRequestDTO request = new ReservaRequestDTO(1L, 42L, Metodo.PIX);
        when(assentoRepository.findById(1L)).thenReturn(Optional.of(assentoLivre));
        when(assentoRepository.save(any(Assento.class))).thenReturn(assentoLivre);

        ReservaResponseDTO response = bookingService.reservarAssento(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.codigoPosicao()).isEqualTo("A5");
        assertThat(response.tipo()).isEqualTo(TipoAssento.VIP);
        assertThat(response.ocupado()).isTrue();

        ArgumentCaptor<Assento> assentoCaptor = ArgumentCaptor.forClass(Assento.class);
        verify(assentoRepository).save(assentoCaptor.capture());
        assertThat(assentoCaptor.getValue().isOcupado()).isTrue();

        ArgumentCaptor<PagamentoMessageDTO> msgCaptor = ArgumentCaptor.forClass(PagamentoMessageDTO.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.FILA_PAGAMENTOS), msgCaptor.capture());
        assertThat(msgCaptor.getValue().reservaId()).isEqualTo(1L);
        assertThat(msgCaptor.getValue().metodo()).isEqualTo(Metodo.PIX);
        assertThat(msgCaptor.getValue().usuarioId()).isEqualTo(42L);
    }

    // -----------------------------------------------------------------------
    // Cenário 2: Assento não encontrado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Deve lançar RegraNegocioException quando assento não for encontrado")
    void deveLancarExcecaoQuandoAssentoNaoEncontrado() {
        ReservaRequestDTO request = new ReservaRequestDTO(99L, 1L, Metodo.CARTAO);
        when(assentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.reservarAssento(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("99");

        verify(assentoRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    // -----------------------------------------------------------------------
    // Cenário 3: Assento já ocupado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Deve lançar RegraNegocioException quando assento já estiver reservado")
    void deveLancarExcecaoQuandoAssentoJaOcupado() {
        assentoLivre.setOcupado(true);
        ReservaRequestDTO request = new ReservaRequestDTO(1L, 1L, Metodo.PIX);
        when(assentoRepository.findById(1L)).thenReturn(Optional.of(assentoLivre));

        assertThatThrownBy(() -> bookingService.reservarAssento(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já está reservado");

        verify(assentoRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    // -----------------------------------------------------------------------
    // Cenário 4: Falha ao publicar na fila RabbitMQ
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Deve lançar RegraNegocioException e NÃO persistir quando RabbitMQ falhar")
    void deveLancarExcecaoQuandoRabbitMQFalhar() {
        ReservaRequestDTO request = new ReservaRequestDTO(1L, 1L, Metodo.PIX);
        when(assentoRepository.findById(1L)).thenReturn(Optional.of(assentoLivre));
        when(assentoRepository.save(any(Assento.class))).thenReturn(assentoLivre);
        doThrow(new AmqpException("Conexão recusada"))
                .when(rabbitTemplate)
                .convertAndSend(eq(RabbitMQConfig.FILA_PAGAMENTOS), any(PagamentoMessageDTO.class));

        assertThatThrownBy(() -> bookingService.reservarAssento(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("indisponível");
    }
}
