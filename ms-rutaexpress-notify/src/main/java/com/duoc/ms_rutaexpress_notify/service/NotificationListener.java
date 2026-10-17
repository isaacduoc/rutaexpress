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
import java.io.UncheckedIOException;

@Service
public class NotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationListener.class);

    private static final int MAX_INTENTOS = 3;
    private static final long ESPERA_MS = 500;

    private final EtiquetaService etiquetaService;
    private final EmailService emailService;

    public NotificationListener(
            EtiquetaService etiquetaService,
            EmailService emailService) {

        this.etiquetaService = etiquetaService;
        this.emailService = emailService;
    }

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

        // Simular errores temporales en pruebas
        if (simularErrorTransitorio
                && mensaje.trim().equals("PRUEBA_REINTENTO")) {

            throw new ErrorTransitorioException(
                    "Fallo temporal simulado"
            );
        }

        // =========================
        // GENERAR ETIQUETA REAL
        // =========================

        if ("LABEL".equals(tipo)) {

            try {

                var archivo = etiquetaService.generarEtiqueta(
                        mensaje
                );

                log.info(
                        "[LABEL] Etiqueta generada correctamente: {}",
                        archivo
                );

            } catch (UncheckedIOException e) {

                throw new ErrorTransitorioException(
                        "Error temporal al guardar etiqueta: "
                        + e.getMessage()
                );
            }

            return;
        }

        // EMAIL: pendiente integrar procesamiento real
        // =========================
        // PREPARAR EMAIL
        // =========================

        if ("EMAIL".equals(tipo)) {

            try {

                var archivo = emailService.prepararEmail(
                        mensaje
                );

                log.info(
                        "[EMAIL] Correo preparado correctamente: {}",
                        archivo
                );

            } catch (UncheckedIOException e) {

                throw new ErrorTransitorioException(
                        "Error temporal al guardar correo: "
                        + e.getMessage()
                );
            }

            return;
        }

        // Rechazar tipos de mensajes desconocidos
        throw new IllegalArgumentException(
                "Tipo de mensaje no soportado: " + tipo
        );


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
