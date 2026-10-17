package com.duoc.ms_rutaexpress_rabbit_admin.controller;

import com.duoc.ms_rutaexpress_rabbit_admin.service.ColaAdminService;

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

    private final ColaAdminService servicio;

    public ColaAdminController(ColaAdminService servicio) {
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
                .body(servicio.crear(request.nombre()));
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<Map<String, Object>> consultar(
            @PathVariable String nombre) {

        return ResponseEntity.ok(
                servicio.consultar(nombre)
        );
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String nombre) {

        servicio.eliminar(nombre);

        return ResponseEntity.noContent().build();
    }
}
