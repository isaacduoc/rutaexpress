
package com.duoc.ms_rutaexpress_notify.service;

import com.duoc.ms_rutaexpress_notify.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DeadLetterListener {

    private static final Logger log =
            LoggerFactory.getLogger(DeadLetterListener.class);

    // =========================
    // CONSUMIDOR DLQ EMAIL
    // =========================

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL_DLQ)
    public void consumirEmailDLQ(
            String mensaje,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        registrarMensajeFallido(
                "EMAIL",
                mensaje,
                channel,
                deliveryTag
        );
    }

    // =========================
    // CONSUMIDOR DLQ WAREHOUSE
    // =========================

    @RabbitListener(queues = RabbitMQConfig.QUEUE_WAREHOUSE_DLQ)
    public void consumirWarehouseDLQ(
            String mensaje,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        registrarMensajeFallido(
                "WAREHOUSE",
                mensaje,
                channel,
                deliveryTag
        );
    }

    // =========================
    // CONSUMIDOR DLQ LABEL
    // =========================

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LABEL_DLQ)
    public void consumirLabelDLQ(
            String mensaje,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        registrarMensajeFallido(
                "LABEL",
                mensaje,
                channel,
                deliveryTag
        );
    }

    // =========================
    // REGISTRAR MENSAJES FALLIDOS
    // =========================

    private void registrarMensajeFallido(
            String tipo,
            String mensaje,
            Channel channel,
            long deliveryTag
    ) throws IOException {

        log.error(
                "[DLQ {}] Mensaje fallido recibido",
                tipo
        );

        log.error(
                "[DLQ {}] Longitud del mensaje: {}",
                tipo,
                mensaje == null ? 0 : mensaje.length()
        );

        log.error(
                "[DLQ {}] Contenido: [{}]",
                tipo,
                mensaje
        );

        // Confirmar el mensaje despues de registrarlo
        channel.basicAck(deliveryTag, false);

        log.info(
                "[DLQ {}] Mensaje registrado y ACK enviado",
                tipo
        );
    }
}
