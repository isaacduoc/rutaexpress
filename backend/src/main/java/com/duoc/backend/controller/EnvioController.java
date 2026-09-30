package com.duoc.backend.controller;

import com.duoc.backend.dto.CrearEnvioRequest;
import com.duoc.backend.dto.CambiarEstadoRequest;
import com.duoc.backend.entity.Envio;
import com.duoc.backend.entity.EstadoEnvio;
import com.duoc.backend.service.EnvioService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(
            EnvioService envioService) {

        this.envioService =
                envioService;
    }


    @PostMapping
    public ResponseEntity<Envio> crearEnvio(
            @RequestBody
            CrearEnvioRequest request) {

        Envio creado =
                envioService.crearEnvio(
                        request
                );

        return new ResponseEntity<>(
                creado,
                HttpStatus.CREATED
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Envio> obtenerPorId(
            @PathVariable
            Long id) {

        return ResponseEntity.ok(
                envioService.obtenerPorId(id)
        );
    }


    /*
     * correoRemitente será agregado por el BFF
     * únicamente cuando el usuario sea Cliente.
     */
    @GetMapping
    public ResponseEntity<List<Envio>> listarEnvios(

            @RequestParam(required = false)
            EstadoEnvio estado,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso =
                    DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso =
                    DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime to,

            @RequestParam(required = false)
            String correoRemitente) {

        return ResponseEntity.ok(
                envioService.listarEnvios(
                        estado,
                        from,
                        to,
                        correoRemitente
                )
        );
    }


    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(

            @PathVariable
            Long id,

            @RequestBody
            CambiarEstadoRequest request) {

        try {

            Envio actualizado =
                    envioService.cambiarEstado(
                            id,
                            request.getEstado()
                    );

            return ResponseEntity.ok(
                    actualizado
            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );
        }
    }


    /*
     * No borra físicamente.
     * Solo deja de aparecer en Envíos.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> archivarEnvio(
            @PathVariable
            Long id) {

        try {

            envioService.archivarEnvio(id);

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );
        }
    }
}