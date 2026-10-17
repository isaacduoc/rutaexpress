
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
@RequestMapping("/api/admin/exchanges")
public class ExchangeAdminController {

    private final RabbitAdminService servicio;

    public ExchangeAdminController(RabbitAdminService servicio) {
        this.servicio = servicio;
    }

    public record CrearExchangeRequest(

            @NotBlank(message = "El nombre es obligatorio")
            @Pattern(
                    regexp = "^ep2\\.admin\\.[a-z0-9._-]{1,50}$",
                    message = "El nombre debe comenzar con ep2.admin."
            )
            String nombre,

            @NotBlank(message = "El tipo es obligatorio")
            @Pattern(
                    regexp = "direct|topic",
                    message = "El tipo debe ser direct o topic"
            )
            String tipo

    ) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(
            @Valid @RequestBody CrearExchangeRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicio.crearExchange(
                        request.nombre(),
                        request.tipo()
                ));
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String nombre) {

        servicio.eliminarExchange(nombre);

        return ResponseEntity.noContent().build();
    }
}
