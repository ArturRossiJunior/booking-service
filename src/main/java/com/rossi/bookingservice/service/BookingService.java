package com.rossi.bookingservice.service;

import com.rossi.bookingservice.config.RabbitMQConfig;
import com.rossi.bookingservice.dto.PagamentoMessageDTO;
import com.rossi.bookingservice.dto.request.ReservaRequestDTO;
import com.rossi.bookingservice.dto.response.ReservaResponseDTO;
import com.rossi.bookingservice.exception.RegraNegocioException;
import com.rossi.bookingservice.model.Assento;
import com.rossi.bookingservice.repository.AssentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final AssentoRepository assentoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ReservaResponseDTO reservarAssento(ReservaRequestDTO request) {

        Assento assento = assentoRepository.findById(request.assentoId())
                .orElseThrow(
                        () -> new RegraNegocioException("Assento não encontrado com o ID: " + request.assentoId()));

        if (assento.isOcupado()) {
            throw new RegraNegocioException("Este assento já está reservado.");
        }

        assento.setOcupado(true);

        Assento assentoSalvo = assentoRepository.save(assento);

        PagamentoMessageDTO mensagem = new PagamentoMessageDTO(
                assentoSalvo.getId(),
                assentoSalvo.getValor(),
                request.metodo(),
                request.usuarioId());

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_PAGAMENTOS, mensagem);
        } catch (AmqpException e) {
            log.error("Falha ao enviar mensagem para fila de pagamentos. Reserva será revertida. Erro: {}", e.getMessage());
            throw new RegraNegocioException("Serviço de pagamento indisponível. Tente novamente em instantes.");
        }

        return converterParaResponseDTO(assentoSalvo);
    }

    private ReservaResponseDTO converterParaResponseDTO(Assento assento) {
        String codigoPosicao = assento.getFileira() + assento.getNumero();

        return new ReservaResponseDTO(
                assento.getId(),
                codigoPosicao,
                assento.getTipo(),
                assento.getValor(),
                assento.isOcupado());
    }
}