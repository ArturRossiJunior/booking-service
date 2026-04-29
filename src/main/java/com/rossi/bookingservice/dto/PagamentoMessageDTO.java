package com.rossi.bookingservice.dto;

import com.rossi.bookingservice.model.enums.Metodo;

import java.math.BigDecimal;

public record PagamentoMessageDTO(Long reservaId,
                                  BigDecimal valor,
                                  Metodo metodo,
                                  Long usuarioId) {
}
