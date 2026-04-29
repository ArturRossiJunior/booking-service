package com.rossi.bookingservice.dto.response;

import com.rossi.bookingservice.model.enums.TipoAssento;

import java.math.BigDecimal;

public record ReservaResponseDTO(Long id,
        String codigoPosicao,
        TipoAssento tipo,
        BigDecimal valor,
        boolean ocupado) {
}