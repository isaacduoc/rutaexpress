
package com.duoc.ms_rutaexpress_notify.service;

import com.duoc.ms_rutaexpress_notify.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationListener.class);

    private static final int MAX_INTENTOS = 3;
    private static final long ESPERA_MS = 500;

    @Value("${rutaexpress.rabbit.pruebas.simular-error-transitorio:false}")
    private boolean simularErrorTransitorio;

    // =========================
    // CONSUMIDOR EMAIL
    // =========================

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void consumirEmail(
            String mensaje,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        procesarMensaje("EMAIL", mensaje, channel, deliveryTag);
    }

    // =========================
    // CONSUMIDOR LABEL
    // =========================

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LABEL)
    public void consumirLabel(
            String mensaje,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        procesarMensaje("LABEL", mensaje, channel, deliveryTag);
    }

    // =========================
    // PROCESAMIENTO CON REINTENTOS
    // =========================

    private void procesarMensaje(
            String tipo,
            String mensaje,
            Channel channel,
            long deliveryTag
    ) throws IOException {

        // Error no recuperable: mensaje vacío
        if (mensaje == null || mensaje.isBlank()) {

            log.error(
                    "[{}] Mensaje vacío. Enviando a DLQ",
                    tipo
            );

            channel.basicNack(deliveryTag, false, false);
            return;
        }

        // Hasta 3 intentos para errores transitorios
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {

            try {

                log.info(
                        "[{}] Procesando mensaje. Intento {}/{}",
                        tipo, intento, MAX_INTENTOS
                );

                ejecutarProcesamiento(tipo, mensaje);

                channel.basicAck(deliveryTag, false);

                log.info(
                        "[{}] Procesamiento correcto. ACK enviado",
                        tipo
                );

                return;

            } catch (ErrorTransitorioException e) {

                log.warn(
                        "[{}] Error recuperable en intento {}: {}",
                        tipo, intento, e.getMessage()
                );

                if (intento == MAX_INTENTOS) {

                    log.error(
                            "[{}] Se agotaron los {} intentos. Enviando a DLQ",
                            tipo, MAX_INTENTOS
                    );

                    channel.basicNack(deliveryTag, false, false);
                    return;
                }

                try {
                    Thread.sleep(ESPERA_MS);

                } catch (InterruptedException e2) {

                    Thread.currentThread().interrupt();

                    log.error(
                            "[{}] Reintentos interrumpidos. Enviando a DLQ",
                            tipo
                    );

                    channel.basicNack(deliveryTag, false, false);
                    return;
                }

            } catch (RuntimeException e) {

                log.error(
                        "[{}] Error no recuperable. Enviando a DLQ",
                        tipo, e
                );

                channel.basicNack(deliveryTag, false, false);
                return;
            }
        }
    }

    // =========================
    // PROCESAMIENTO DEL MENSAJE
    // =========================

    private void ejecutarProcesamiento(
            String tipo,
            String mensaje
    ) {

        // Simulación activable solo para pruebas locales
        if (simularErrorTransitorio
                && mensaje.trim().equals("PRUEBA_REINTENTO")) {

            throw new ErrorTransitorioException(
                    "Fallo temporal simulado"
            );
        }

        // Procesamiento inicial.
        // Aquí integraremos después la lógica real.
        log.info(
                "[{}] Mensaje recibido: {}",
                tipo, mensaje
        );
    }

    // =========================
    // EXCEPCIÓN RECUPERABLE
    // =========================

    private static class ErrorTransitorioException
            extends RuntimeException {

        public ErrorTransitorioException(String mensaje) {
            super(mensaje);
        }
    }
}
