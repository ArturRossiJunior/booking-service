package com.rossi.bookingservice.repository;

import com.rossi.bookingservice.model.Assento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssentoRepository extends JpaRepository<Assento, Long> {
    Optional<Assento> findByFileiraAndNumero(String fileira, Integer numero);
}
