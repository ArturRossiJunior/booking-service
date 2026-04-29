package com.rossi.bookingservice.dto;

import com.rossi.bookingservice.model.enums.StatusPagamento;

public record ConfirmacaoMessageDTO(Long reservaId,
                                    StatusPagamento status) {
}
