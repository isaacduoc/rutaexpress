package com.duoc.ms_rutaexpress_rabbit_admin.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class BindingAdminService {

    private final AmqpAdmin admin;

    public BindingAdminService(AmqpAdmin admin) {
        this.admin = admin;
    }

    private void validarRecurso(String nombre) {

        if (nombre == null ||
                !nombre.matches("^ep2\\.admin\\.[a-z0-9._-]{1,50}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten recursos ep2.admin.*"
            );
        }
    }

    private void validarRoutingKey(String routingKey) {

        if (routingKey == null ||
                !routingKey.matches("^ep2\\.[a-z0-9._*#-]{1,60}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La routing key debe comenzar con ep2."
            );
        }
    }

    private Binding construirBinding(
            String cola,
            String exchange,
            String routingKey) {

        return new Binding(
                cola,
                Binding.DestinationType.QUEUE,
                exchange,
                routingKey,
                null
        );
    }

    public Map<String, String> crear(
            String cola,
            String exchange,
            String routingKey) {

        validarRecurso(cola);
        validarRecurso(exchange);
        validarRoutingKey(routingKey);

        try {

            if (admin.getQueueProperties(cola) == null) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cola no existe"
                );
            }

            Binding binding = construirBinding(
                    cola,
                    exchange,
                    routingKey
            );

            admin.declareBinding(binding);

            return Map.of(
                    "cola", cola,
                    "exchange", exchange,
                    "routingKey", routingKey,
                    "resultado", "Binding declarado correctamente"
            );

        } catch (AmqpException e) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible crear el binding en RabbitMQ",
                    e
            );
        }
    }

    public void eliminar(
            String cola,
            String exchange,
            String routingKey) {

        validarRecurso(cola);
        validarRecurso(exchange);
        validarRoutingKey(routingKey);

        try {

            Binding binding = construirBinding(
                    cola,
                    exchange,
                    routingKey
            );

            admin.removeBinding(binding);

        } catch (AmqpException e) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible eliminar el binding",
                    e
            );
        }
    }
}
