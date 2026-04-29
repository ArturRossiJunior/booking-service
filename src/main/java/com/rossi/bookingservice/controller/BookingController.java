package com.rossi.bookingservice.controller;

import com.rossi.bookingservice.dto.request.ReservaRequestDTO;
import com.rossi.bookingservice.dto.response.ReservaResponseDTO;
import com.rossi.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> reservarAssento(@Valid @RequestBody ReservaRequestDTO request) {

        ReservaResponseDTO response = bookingService.reservarAssento(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
