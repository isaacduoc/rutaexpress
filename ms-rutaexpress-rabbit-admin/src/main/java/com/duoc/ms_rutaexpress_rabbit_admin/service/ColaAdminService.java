package com.duoc.ms_rutaexpress_rabbit_admin.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Properties;

@Service
public class ColaAdminService {

    private final AmqpAdmin admin;

    public ColaAdminService(AmqpAdmin admin) {
        this.admin = admin;
    }

    private void validarNombre(String nombre) {
        if (nombre == null ||
                !nombre.matches("^ep2\\.admin\\.[a-z0-9._-]{1,50}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten colas ep2.admin.*"
            );
        }
    }

    public Map<String, Object> crear(String nombre) {

        validarNombre(nombre);

        try {
            if (admin.getQueueProperties(nombre) != null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La cola ya existe"
                );
            }

            admin.declareQueue(
                    QueueBuilder.durable(nombre).build()
            );

            return Map.of(
                    "nombre", nombre,
                    "resultado", "Cola creada correctamente"
            );

        } catch (AmqpException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo crear la cola en RabbitMQ",
                    e
            );
        }
    }

    public Map<String, Object> consultar(String nombre) {

        validarNombre(nombre);

        try {
            Properties propiedades =
                    admin.getQueueProperties(nombre);

            if (propiedades == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cola no existe"
                );
            }

            return Map.of(
                    "nombre", nombre,
                    "mensajes", propiedades.get(
                            RabbitAdmin.QUEUE_MESSAGE_COUNT
                    ),
                    "consumidores", propiedades.get(
                            RabbitAdmin.QUEUE_CONSUMER_COUNT
                    )
            );

        } catch (AmqpException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar RabbitMQ",
                    e
            );
        }
    }

    public void eliminar(String nombre) {

        validarNombre(nombre);

        try {
            if (admin.getQueueProperties(nombre) == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cola no existe"
                );
            }

            if (!admin.deleteQueue(nombre)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No se pudo eliminar la cola"
                );
            }

        } catch (AmqpException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Error al eliminar cola en RabbitMQ",
                    e
            );
        }
    }
}
