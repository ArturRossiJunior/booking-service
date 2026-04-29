package com.rossi.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rossi.bookingservice.dto.request.ReservaRequestDTO;
import com.rossi.bookingservice.dto.response.ReservaResponseDTO;
import com.rossi.bookingservice.exception.RegraNegocioException;
import com.rossi.bookingservice.model.enums.Metodo;
import com.rossi.bookingservice.model.enums.TipoAssento;
import com.rossi.bookingservice.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingController — Testes de Slice Web")
class BookingControllerTest {

        private MockMvc mockMvc;

        private ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private BookingService bookingService;

        @InjectMocks
        private BookingController bookingController;

        private static final String URL = "/api/reservas";

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                                .setControllerAdvice(new com.rossi.bookingservice.exception.GlobalExceptionHandler())
                                .build();
        }

        // -----------------------------------------------------------------------
        // Cenário 1: Requisição válida → 201 Created
        // -----------------------------------------------------------------------

        @Test
        @DisplayName("POST /api/reservas com dados válidos deve retornar 201 Created com body correto")
        void deveRetornar201QuandoRequisicaoValida() throws Exception {
                ReservaRequestDTO request = new ReservaRequestDTO(1L, 42L, Metodo.PIX);
                ReservaResponseDTO responseEsperado = new ReservaResponseDTO(1L, "A5", TipoAssento.VIP,
                                new BigDecimal("50.00"),
                                true);
                when(bookingService.reservarAssento(any(ReservaRequestDTO.class))).thenReturn(responseEsperado);

                mockMvc.perform(post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.codigoPosicao").value("A5"))
                                .andExpect(jsonPath("$.tipo").value("VIP"))
                                .andExpect(jsonPath("$.ocupado").value(true));
        }

        // -----------------------------------------------------------------------
        // Cenário 2: Body inválido (campo nulo) → 400 Bad Request
        // -----------------------------------------------------------------------

        @Test
        @DisplayName("POST /api/reservas com assentoId nulo deve retornar 400 Bad Request")
        void deveRetornar400QuandoAssentoIdNulo() throws Exception {
                String bodyInvalido = """
                                {
                                    "usuarioId": 42,
                                    "metodo": "PIX"
                                }
                                """;

                mockMvc.perform(post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyInvalido))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("POST /api/reservas com metodo nulo deve retornar 400 Bad Request")
        void deveRetornar400QuandoMetodoNulo() throws Exception {
                String bodyInvalido = """
                                {
                                    "assentoId": 1,
                                    "usuarioId": 42
                                }
                                """;

                mockMvc.perform(post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyInvalido))
                                .andExpect(status().isBadRequest());
        }

        // -----------------------------------------------------------------------
        // Cenário 3: Service lança RegraNegocioException → 400 Bad Request
        // -----------------------------------------------------------------------

        @Test
        @DisplayName("POST /api/reservas deve retornar 400 quando assento já estiver ocupado")
        void deveRetornar400QuandoAssentoJaOcupado() throws Exception {
                ReservaRequestDTO request = new ReservaRequestDTO(1L, 42L, Metodo.PIX);
                when(bookingService.reservarAssento(any(ReservaRequestDTO.class)))
                                .thenThrow(new RegraNegocioException("Este assento já está reservado."));

                mockMvc.perform(post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.mensagem").value("Este assento já está reservado."));
        }
}
