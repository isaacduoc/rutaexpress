package com.duoc.ms_rutaexpress_catalog.repository;

import com.duoc.ms_rutaexpress_catalog.entity.TicketBodega;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketBodegaRepository
        extends JpaRepository<TicketBodega, Long> {

    // Buscar un ticket por el ID del envio
    Optional<TicketBodega> findByEnvioId(Long envioId);

    // Verificar si ya existe un ticket para el envio
    boolean existsByEnvioId(Long envioId);
}
