
package com.duoc.backend.service;

import com.duoc.backend.config.RabbitMQConfig;
import com.duoc.backend.entity.Envio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public NotificationPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {

        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    // =========================
    // PUBLICAR CAMBIO DE ESTADO
    // =========================

    public void enviarNotificacionCambioEstado(Envio envio) {

        // Validar que el envio exista y este guardado
        if (envio == null || envio.getId() == null) {
            throw new IllegalArgumentException(
                "El envío debe estar guardado antes de publicar"
            );
        }

        // Construir los datos del mensaje
        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", envio.getId());
        datos.put("estado", envio.getEstado());
        datos.put("destinatario", envio.getNombreDestinatario());
        datos.put("correoDestinatario", envio.getCorreoDestinatario());
        datos.put("codigoSeguimiento", envio.getCodigoSeguimiento());

        // Convertir los datos a JSON
        String mensaje;

        try {
            mensaje = objectMapper.writeValueAsString(datos);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "Error al generar el mensaje JSON", e
            );
        }

        // =========================
        // PUBLICAR NOTIFICACION EMAIL
        // =========================

        publicarMensaje(
            RabbitMQConfig.ROUTING_KEY_EMAIL,
            mensaje
        );

        System.out.println(
            ">>> [BACKEND] Publicación RabbitMQ solicitada para envío ID: "
            + envio.getId()
        );
    }


    // =========================
    // PUBLICAR COMANDO WAREHOUSE
    // =========================

    public void publicarComandoBodega(Envio envio) {

        // Validar que el envio ya este guardado
        if (envio == null || envio.getId() == null) {
            throw new IllegalArgumentException(
                "El envio debe estar guardado antes de publicar Warehouse"
            );
        }

        // Construir los datos que necesita Catalog
        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("envioId", envio.getId());
        datos.put("codigoSeguimiento", envio.getCodigoSeguimiento());
        datos.put("servicio", envio.getServicio());
        datos.put("direccionDestino", envio.getDireccionDestino());

        // Convertir los datos a JSON
        String mensaje;

        try {
            mensaje = objectMapper.writeValueAsString(datos);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "Error al generar el comando Warehouse", e
            );
        }

        // Publicar hacia la cola Warehouse mediante el exchange
        publicarMensaje(
            RabbitMQConfig.ROUTING_KEY_WAREHOUSE,
            mensaje
        );

        System.out.println(
            ">>> [BACKEND] Comando Warehouse publicado para envio ID: "
            + envio.getId()
        );
    }


    // =========================
    // PUBLICAR COMANDO LABEL
    // =========================

    public void publicarComandoEtiqueta(Envio envio) {

        if (envio == null || envio.getId() == null) {
            throw new IllegalArgumentException(
                "El envio debe estar guardado antes de generar etiqueta"
            );
        }

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("envioId", envio.getId());
        datos.put("codigoSeguimiento", envio.getCodigoSeguimiento());
        datos.put("destinatario", envio.getNombreDestinatario());
        datos.put("direccionDestino", envio.getDireccionDestino());
        datos.put("servicio", envio.getServicio());
        datos.put("tipoPaquete", envio.getTipoPaquete());
        datos.put("peso", envio.getPeso());

        String mensaje;

        try {
            mensaje = objectMapper.writeValueAsString(datos);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "Error al generar comando Label", e
            );
        }

        publicarMensaje(
            RabbitMQConfig.ROUTING_KEY_LABEL,
            mensaje
        );

        System.out.println(
            ">>> [BACKEND] Comando Label solicitado para envio ID: "
            + envio.getId()
        );
    }


    // =========================
    // METODO PARA PUBLICAR
    // =========================

    private void publicarMensaje(
            String routingKey,
            String mensaje) {

        MessageProperties propiedades = new MessageProperties();

        // El consumidor actual recibe String
        propiedades.setContentType(
            MessageProperties.CONTENT_TYPE_TEXT_PLAIN
        );

        propiedades.setContentEncoding("UTF-8");

        // Solicitar mensajes persistentes
        propiedades.setDeliveryMode(
            MessageDeliveryMode.PERSISTENT
        );

        // Crear mensaje
        Message message = new Message(
            mensaje.getBytes(StandardCharsets.UTF_8),
            propiedades
        );

        // Enviar mensaje a RabbitMQ
        rabbitTemplate.send(
            RabbitMQConfig.EXCHANGE_DIRECT,
            routingKey,
            message
        );

        System.out.println(
            ">>> [RABBITMQ] Publicación solicitada: "
            + routingKey
        );
    }
}
