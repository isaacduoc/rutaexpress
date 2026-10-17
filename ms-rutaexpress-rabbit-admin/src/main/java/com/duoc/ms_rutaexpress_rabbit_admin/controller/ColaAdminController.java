
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
@RequestMapping("/api/admin/colas")
public class ColaAdminController {

    private final RabbitAdminService servicio;

    public ColaAdminController(RabbitAdminService servicio) {
        this.servicio = servicio;
    }

    public record CrearColaRequest(

        @NotBlank(message = "El nombre es obligatorio")

        @Pattern(
            regexp = "^ep2\\.admin\\.[a-z0-9._-]{1,50}$",
            message = "El nombre debe comenzar con ep2.admin."
        )
        String nombre

    ) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(
            @Valid @RequestBody CrearColaRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicio.crearCola(request.nombre()));
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<Map<String, Object>> consultar(
            @PathVariable String nombre) {

        return ResponseEntity.ok(
                servicio.consultarCola(nombre)
        );
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String nombre) {

        servicio.eliminarCola(nombre);

        return ResponseEntity.noContent().build();
    }
}
