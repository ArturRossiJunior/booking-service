package com.rossi.bookingservice.exception;

import java.time.LocalDateTime;

public record ErroResponse (LocalDateTime timestamp,
                            Integer status,
                            String erro,
                            String mensagem) {
}
