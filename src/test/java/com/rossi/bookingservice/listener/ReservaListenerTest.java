package com.rossi.bookingservice.listener;

import com.rossi.bookingservice.dto.ConfirmacaoMessageDTO;
import com.rossi.bookingservice.exception.RegraNegocioException;
import com.rossi.bookingservice.model.Assento;
import com.rossi.bookingservice.model.enums.StatusPagamento;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaListener — Testes Unitários")
class ReservaListenerTest {

    @Mock
    private AssentoRepository assentoRepository;

    @InjectMocks
    private ReservaListener reservaListener;

    private Assento assentoOcupado;

    @BeforeEach
    void setUp() {
        assentoOcupado = new Assento();
        assentoOcupado.setId(10L);
        assentoOcupado.setFileira("B");
        assentoOcupado.setNumero(3);
        assentoOcupado.setTipo(TipoAssento.NORMAL);
        assentoOcupado.setValor(new BigDecimal("25.00"));
        assentoOcupado.setOcupado(true);
    }

    // -----------------------------------------------------------------------
    // Cenário 1: Pagamento RECUSADO → assento deve ser liberado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Pagamento RECUSADO deve setar ocupado=false e salvar o assento")
    void deveLiberarAssentoQuandoPagamentoRecusado() {
        ConfirmacaoMessageDTO confirmacao = new ConfirmacaoMessageDTO(10L, StatusPagamento.RECUSADO);
        when(assentoRepository.findById(10L)).thenReturn(Optional.of(assentoOcupado));
        when(assentoRepository.save(any(Assento.class))).thenReturn(assentoOcupado);

        reservaListener.processarRespostaPagamento(confirmacao);

        ArgumentCaptor<Assento> captor = ArgumentCaptor.forClass(Assento.class);
        verify(assentoRepository).save(captor.capture());
        assertThat(captor.getValue().isOcupado()).isFalse();
    }

    // -----------------------------------------------------------------------
    // Cenário 2: Pagamento APROVADO → assento NÃO deve ser alterado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Pagamento APROVADO não deve buscar nem alterar o assento")
    void naoDeveAlterarAssentoQuandoPagamentoAprovado() {
        ConfirmacaoMessageDTO confirmacao = new ConfirmacaoMessageDTO(10L, StatusPagamento.APROVADO);

        reservaListener.processarRespostaPagamento(confirmacao);

        verifyNoInteractions(assentoRepository);
    }

    // -----------------------------------------------------------------------
    // Cenário 3: Pagamento RECUSADO mas assento não existe → exceção
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Pagamento RECUSADO com assento inexistente deve lançar RegraNegocioException")
    void deveLancarExcecaoQuandoAssentoNaoEncontradoAoRecusar() {
        ConfirmacaoMessageDTO confirmacao = new ConfirmacaoMessageDTO(999L, StatusPagamento.RECUSADO);
        when(assentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaListener.processarRespostaPagamento(confirmacao))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("999");

        verify(assentoRepository, never()).save(any());
    }
}
