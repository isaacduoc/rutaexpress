package com.duoc.ms_rutaexpress_catalog.service;

import com.duoc.ms_rutaexpress_catalog.entity.TicketBodega;
import com.duoc.ms_rutaexpress_catalog.repository.TicketBodegaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketBodegaService {

    private static final Logger log =
            LoggerFactory.getLogger(TicketBodegaService.class);

    private final TicketBodegaRepository repository;

    public TicketBodegaService(
            TicketBodegaRepository repository) {
        this.repository = repository;
    }

    // =========================================
    // REGISTRAR TICKET DE BODEGA
    // =========================================

    @Transactional
    public TicketBodega registrarTicket(
            Long envioId,
            String codigoSeguimiento,
            String servicio,
            String direccionDestino) {

        // Validar identificador del envio
        if (envioId == null || envioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del envio es obligatorio y debe ser positivo"
            );
        }

        // Validar campos obligatorios
        codigoSeguimiento = validarTexto(
                codigoSeguimiento, "codigoSeguimiento", 120
        );

        servicio = validarTexto(
                servicio, "servicio", 255
        );

        direccionDestino = validarTexto(
                direccionDestino, "direccionDestino", 255
        );

        // Evitar duplicados en procesamiento secuencial
        var ticketExistente =
                repository.findByEnvioId(envioId);

        if (ticketExistente.isPresent()) {

            log.info(
                    "[WAREHOUSE] El envio {} ya tiene un ticket",
                    envioId
            );

            return ticketExistente.get();
        }

        // Crear ticket de bodega
        TicketBodega ticket = new TicketBodega(
                envioId,
                codigoSeguimiento,
                servicio,
                direccionDestino
        );

        // Guardar utilizando JPA
        TicketBodega guardado =
                repository.saveAndFlush(ticket);

        log.info(
                "[WAREHOUSE] Ticket registrado para envio {}",
                envioId
        );

        return guardado;
    }

    // =========================================
    // VALIDAR TEXTO OBLIGATORIO
    // =========================================

    private String validarTexto(
            String valor,
            String campo,
            int longitudMaxima) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " es obligatorio"
            );
        }

        String limpio = valor.trim();

        if (limpio.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    "El campo " + campo
                    + " supera la longitud maxima de "
                    + longitudMaxima
            );
        }

        return limpio;
    }
}
