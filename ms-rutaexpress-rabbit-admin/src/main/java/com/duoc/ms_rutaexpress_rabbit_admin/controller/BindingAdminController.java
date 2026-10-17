
package com.duoc.ms_rutaexpress_rabbit_admin.controller;

import com.duoc.ms_rutaexpress_rabbit_admin.service.RabbitAdminService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/bindings")
public class BindingAdminController {

    private final RabbitAdminService servicio;

    public BindingAdminController(RabbitAdminService servicio) {
        this.servicio = servicio;
    }

    public record BindingRequest(

            @NotBlank(message = "El nombre de la cola es obligatorio")
            @Pattern(
                    regexp = "^ep2\\.admin\\.[a-z0-9._-]{1,50}$",
                    message = "La cola debe comenzar con ep2.admin."
            )
            String cola,

            @NotBlank(message = "El nombre del exchange es obligatorio")
            @Pattern(
                    regexp = "^ep2\\.admin\\.[a-z0-9._-]{1,50}$",
                    message = "El exchange debe comenzar con ep2.admin."
            )
            String exchange,

            @NotBlank(message = "La routing key es obligatoria")
            @Pattern(
                    regexp = "^ep2\\.[a-z0-9._*#-]{1,60}$",
                    message = "La routing key debe comenzar con ep2."
            )
            String routingKey

    ) {}

    @PostMapping
    public ResponseEntity<Map<String, String>> crear(
            @Valid @RequestBody BindingRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicio.crearBinding(
                        request.cola(),
                        request.exchange(),
                        request.routingKey()
                ));
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminar(
            @Valid @RequestBody BindingRequest request) {

        servicio.eliminarBinding(
                request.cola(),
                request.exchange(),
                request.routingKey()
        );

        return ResponseEntity.noContent().build();
    }
}
