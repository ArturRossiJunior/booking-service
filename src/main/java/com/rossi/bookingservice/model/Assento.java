package com.rossi.bookingservice.model;

import com.rossi.bookingservice.model.enums.TipoAssento;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "assentos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Assento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileira;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoAssento tipo;

    @Column(nullable = false)
    private BigDecimal valor;

    private boolean ocupado = false;

    @Version
    private Long version;
}