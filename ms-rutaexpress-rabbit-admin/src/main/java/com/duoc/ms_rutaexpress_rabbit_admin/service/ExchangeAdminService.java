package com.duoc.ms_rutaexpress_rabbit_admin.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class ExchangeAdminService {

    private final AmqpAdmin admin;

    public ExchangeAdminService(AmqpAdmin admin) {
        this.admin = admin;
    }

    private void validarNombre(String nombre) {

        if (nombre == null ||
                !nombre.matches("^ep2\\.admin\\.[a-z0-9._-]{1,50}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten exchanges ep2.admin.*"
            );
        }
    }

    public Map<String, Object> crear(
            String nombre,
            String tipo) {

        validarNombre(nombre);

        if (tipo == null ||
                (!tipo.equals("direct") &&
                 !tipo.equals("topic"))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo debe ser direct o topic"
            );
        }

        try {

            if ("direct".equals(tipo)) {

                admin.declareExchange(
                        new DirectExchange(nombre, true, false)
                );

            } else {

                admin.declareExchange(
                        new TopicExchange(nombre, true, false)
                );
            }

            return Map.of(
                    "nombre", nombre,
                    "tipo", tipo,
                    "resultado", "Exchange declarado correctamente"
            );

        } catch (AmqpException e) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Error al crear exchange en RabbitMQ",
                    e
            );
        }
    }

    public void eliminar(String nombre) {

        validarNombre(nombre);

        try {

            boolean eliminado = admin.deleteExchange(nombre);

            if (!eliminado) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exchange no encontrado o no eliminado"
                );
            }

        } catch (AmqpException e) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Error al eliminar exchange",
                    e
            );
        }
    }
}
