package com.rossi.bookingservice.listener;

import com.rossi.bookingservice.config.RabbitMQConfig;
import com.rossi.bookingservice.dto.ConfirmacaoMessageDTO;
import com.rossi.bookingservice.exception.RegraNegocioException;
import com.rossi.bookingservice.model.Assento;
import com.rossi.bookingservice.model.enums.StatusPagamento;
import com.rossi.bookingservice.repository.AssentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservaListener {

    private final AssentoRepository assentoRepository;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.FILA_RESERVAS)
    public void processarRespostaPagamento(ConfirmacaoMessageDTO confirmacao) {

        if (StatusPagamento.RECUSADO == confirmacao.status()) {

            Assento assento = assentoRepository.findById(confirmacao.reservaId())
                    .orElseThrow(() -> new RegraNegocioException("Assento não encontrado com ID: " + confirmacao.reservaId()));

            assento.setOcupado(false);
            assentoRepository.save(assento);

            log.warn("Pagamento recusado. Assento {} liberado.", assento.getId());
        } else {
            log.info("Pagamento confirmado. Assento ID: {} | Status: {}", confirmacao.reservaId(), confirmacao.status());
        }
    }
}