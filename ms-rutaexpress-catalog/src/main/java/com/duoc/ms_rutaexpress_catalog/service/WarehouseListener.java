package com.duoc.ms_rutaexpress_catalog.service;

import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;

import org.springframework.stereotype.Service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class WarehouseListener {

    private static final Logger log =
            LoggerFactory.getLogger(WarehouseListener.class);

    private static final int MAX_INTENTOS = 3;

    private final TicketBodegaService ticketService;
    private final JsonMapper jsonMapper;

    public WarehouseListener(
            TicketBodegaService ticketService,
            JsonMapper jsonMapper) {

        this.ticketService = ticketService;
        this.jsonMapper = jsonMapper;
    }

    // Datos que debe enviar Shipments
    public record ComandoBodega(
            Long envioId,
            String codigoSeguimiento,
            String servicio,
            String direccionDestino
    ) {}

    // ====================================
    // CONSUMIDOR DE WAREHOUSE
    // ====================================

    @RabbitListener(queues = "${rutaexpress.rabbitmq.queue.warehouse}")
    public void consumirWarehouse(
            Message message,
            Channel channel
    ) throws IOException {

        long deliveryTag =
                message.getMessageProperties().getDeliveryTag();

        String contenido = new String(
                message.getBody(),
                StandardCharsets.UTF_8
        );

        // Rechazar mensajes vacios
        if (contenido.isBlank()) {

            log.error("[WAREHOUSE] Mensaje vacio");

            channel.basicNack(deliveryTag, false, false);
            return;
        }

        ComandoBodega comando;

        // Convertir JSON a comando
        try {

            comando = jsonMapper.readValue(
                    contenido,
                    ComandoBodega.class
            );

            if (comando == null) {
                throw new IllegalArgumentException(
                        "El comando no puede ser nulo"
                );
            }

        } catch (JacksonException | IllegalArgumentException e) {

            log.error(
                    "[WAREHOUSE] JSON invalido: {}",
                    e.getMessage()
            );

            channel.basicNack(deliveryTag, false, false);
            return;
        }

        // ====================================
        // PROCESAMIENTO CON REINTENTOS
        // ====================================

        for (int intento = 1;
             intento <= MAX_INTENTOS;
             intento++) {

            try {

                log.info(
                        "[WAREHOUSE] Procesando envio {}. Intento {}/{}",
                        comando.envioId(),
                        intento,
                        MAX_INTENTOS
                );

                // Registrar el ticket en Oracle
                ticketService.registrarTicket(
                        comando.envioId(),
                        comando.codigoSeguimiento(),
                        comando.servicio(),
                        comando.direccionDestino()
                );

                // Confirmar solo despues del servicio
                channel.basicAck(deliveryTag, false);

                log.info(
                        "[WAREHOUSE] ACK enviado para envio {}",
                        comando.envioId()
                );

                return;

            } catch (TransientDataAccessException e) {

                log.warn(
                        "[WAREHOUSE] Error temporal. Intento {}: {}",
                        intento,
                        e.getMessage()
                );

                if (intento == MAX_INTENTOS) {

                    log.error(
                            "[WAREHOUSE] Reintentos agotados. Enviando a DLQ"
                    );

                    channel.basicNack(deliveryTag, false, false);
                    return;
                }

                try {

                    Thread.sleep(500);

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    channel.basicNack(deliveryTag, false, false);
                    return;
                }

            } catch (IllegalArgumentException | DataAccessException e) {

                log.error(
                        "[WAREHOUSE] Error no recuperable: {}",
                        e.getMessage()
                );

                channel.basicNack(deliveryTag, false, false);
                return;

            } catch (RuntimeException e) {

                log.error(
                        "[WAREHOUSE] Fallo inesperado",
                        e
                );

                channel.basicNack(deliveryTag, false, false);
                return;
            }
        }
    }
}
