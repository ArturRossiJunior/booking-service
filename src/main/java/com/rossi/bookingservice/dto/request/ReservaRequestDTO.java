package com.rossi.bookingservice.dto.request;

import com.rossi.bookingservice.model.enums.Metodo;
import jakarta.validation.constraints.NotNull;

public record ReservaRequestDTO (@NotNull Long assentoId,
                                 @NotNull Long usuarioId,
                                 @NotNull Metodo metodo) {
}
