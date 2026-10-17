package com.duoc.ms_rutaexpress_rabbit_admin.controller;

import com.duoc.ms_rutaexpress_rabbit_admin.service.BindingAdminService;

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

    private final BindingAdminService servicio;

    public BindingAdminController(
            BindingAdminService servicio) {

        this.servicio = servicio;
    }

    public record BindingRequest(

            @NotBlank
            @Pattern(
                    regexp = "^ep2\\.admin\\.[a-z0-9._-]{1,50}$"
            )
            String cola,

            @NotBlank
            @Pattern(
                    regexp = "^ep2\\.admin\\.[a-z0-9._-]{1,50}$"
            )
            String exchange,

            @NotBlank
            @Pattern(
                    regexp = "^ep2\\.[a-z0-9._*#-]{1,60}$"
            )
            String routingKey

    ) {}

    @PostMapping
    public ResponseEntity<Map<String, String>> crear(
            @Valid @RequestBody BindingRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicio.crear(
                        request.cola(),
                        request.exchange(),
                        request.routingKey()
                ));
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminar(
            @Valid @RequestBody BindingRequest request) {

        servicio.eliminar(
                request.cola(),
                request.exchange(),
                request.routingKey()
        );

        return ResponseEntity.noContent().build();
    }
}
