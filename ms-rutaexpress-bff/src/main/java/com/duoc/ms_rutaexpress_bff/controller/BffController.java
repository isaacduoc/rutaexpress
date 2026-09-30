package com.duoc.ms_rutaexpress_bff.controller;

import com.duoc.ms_rutaexpress_bff.dto.CambiarEstadoBffRequest;
import com.duoc.ms_rutaexpress_bff.dto.CrearEnvioBffRequest;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bff/v1/shipments")
public class BffController {

    private final RestTemplate restTemplate;

    @Value("${shipments.service.url}")
    private String shipmentsUrl;

    public BffController(
            RestTemplate restTemplate) {

        this.restTemplate =
                restTemplate;
    }


    // =====================================================
    // LISTAR ENVÍOS
    // Cliente -> solo sus propios envíos
    // Admin/Despachador -> todos
    // =====================================================

    @GetMapping
public ResponseEntity<Object> listarEnvios(

        @AuthenticationPrincipal
        Jwt jwt,

        @RequestHeader(
            value = HttpHeaders.AUTHORIZATION,
            required = false
        )
        String authHeader) {

    HttpHeaders headers =
            crearHeadersConToken(
                    authHeader
            );

    HttpEntity<Void> entity =
            new HttpEntity<>(
                    headers
            );

    /*
     * Cliente:
     * solo puede consultar sus propios envíos.
     */
    if (tieneRol(jwt, "Cliente")) {

        String correo =
                obtenerCorreoUsuario(jwt);

        URI uri =
                UriComponentsBuilder
                        .fromUriString(
                                shipmentsUrl
                        )
                        .queryParam(
                                "correoRemitente",
                                correo
                        )
                        .build()
                        .encode()
                        .toUri();

        System.out.println(
                "Cliente consultando envíos: "
                + correo
        );

        return restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                Object.class
        );
    }

    /*
     * Admin / Despachador:
     * reciben todos los envíos.
     */
    return restTemplate.exchange(
            shipmentsUrl,
            HttpMethod.GET,
            entity,
            Object.class
    );
}


    // =====================================================
    // CREAR ENVÍO
    // Cliente -> correo remitente viene del JWT
    // =====================================================

    @PostMapping
    public ResponseEntity<Object> crearEnvio(

            @RequestBody
            CrearEnvioBffRequest request,

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestHeader(
                value = HttpHeaders.AUTHORIZATION,
                required = false
            )
            String authHeader) {

        /*
         * Si es Cliente, nunca confiamos
         * en el correo enviado por Angular.
         *
         * Se fuerza el usuario autenticado.
         */
        if (tieneRol(jwt, "Cliente")) {

            String correo =
                    obtenerCorreoUsuario(jwt);

            request.setCorreoRemitente(
                    correo
            );
        }

        HttpHeaders headers =
                crearHeadersConToken(
                        authHeader
                );

        HttpEntity<CrearEnvioBffRequest> entity =
                new HttpEntity<>(
                        request,
                        headers
                );

        return restTemplate.postForEntity(
                shipmentsUrl,
                entity,
                Object.class
        );
    }


    // =====================================================
    // OBTENER UN ENVÍO POR ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<Object> obtenerEnvio(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestHeader(
                value = HttpHeaders.AUTHORIZATION,
                required = false
            )
            String authHeader) {

        String url =
                shipmentsUrl
                + "/"
                + id;

        HttpHeaders headers =
                crearHeadersConToken(
                        authHeader
                );

        HttpEntity<Void> entity =
                new HttpEntity<>(
                        headers
                );

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

        /*
         * Si es Cliente, también comprobamos
         * que el envío realmente le pertenezca.
         *
         * Esto evita que escriba manualmente:
         *
         * /shipments/123
         *
         * e intente consultar un envío ajeno.
         */
        if (tieneRol(jwt, "Cliente")) {

            Map body =
                    response.getBody();

            if (body == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            Object correoRemitente =
                    body.get(
                            "correoRemitente"
                    );

            String correoUsuario =
                    obtenerCorreoUsuario(jwt);

            if (
                    correoRemitente == null
                    ||
                    !correoUsuario.equalsIgnoreCase(
                            correoRemitente.toString()
                    )
            ) {

                return ResponseEntity
                        .status(403)
                        .body(
                            "No tienes permiso para consultar este envío."
                        );
            }
        }

        return ResponseEntity
                .status(
                        response.getStatusCode()
                )
                .body(
                        response.getBody()
                );
    }


    // =====================================================
    // CAMBIAR ESTADO
    // Spring Security limita esto a
    // Admin / Despachador
    // =====================================================

    @PutMapping("/{id}/estado")
    public ResponseEntity<Object> cambiarEstado(

            @PathVariable
            Long id,

            @RequestBody
            CambiarEstadoBffRequest request,

            @RequestHeader(
                value = HttpHeaders.AUTHORIZATION,
                required = false
            )
            String authHeader) {

        String url =
                shipmentsUrl
                + "/"
                + id
                + "/estado";

        HttpHeaders headers =
                crearHeadersConToken(
                        authHeader
                );

        HttpEntity<CambiarEstadoBffRequest> entity =
                new HttpEntity<>(
                        request,
                        headers
                );

        return restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                Object.class
        );
    }


    // =====================================================
    // ARCHIVAR ENVÍO
    // No lo borra físicamente.
    // Backend solo permite ENTREGADO/CANCELADO.
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> archivarEnvio(

            @PathVariable
            Long id,

            @RequestHeader(
                value = HttpHeaders.AUTHORIZATION,
                required = false
            )
            String authHeader) {

        String url =
                shipmentsUrl
                + "/"
                + id;

        HttpHeaders headers =
                crearHeadersConToken(
                        authHeader
                );

        HttpEntity<Void> entity =
                new HttpEntity<>(
                        headers
                );

        return restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                Object.class
        );
    }


    // =====================================================
    // UTILIDADES JWT
    // =====================================================

    private boolean tieneRol(
            Jwt jwt,
            String rol) {

        if (jwt == null) {
            return false;
        }

        List<String> roles =
                jwt.getClaimAsStringList(
                        "roles"
                );

        return roles != null
                &&
                roles.contains(rol);
    }


    private String obtenerCorreoUsuario(
            Jwt jwt) {

        if (jwt == null) {

            throw new IllegalStateException(
                    "No se encontró el JWT del usuario."
            );
        }

        /*
         * En tokens de Microsoft Entra suele
         * venir preferred_username.
         */
        String correo =
                jwt.getClaimAsString(
                        "preferred_username"
                );

        /*
         * Fallback por si el tenant entrega
         * otro claim.
         */
        if (
                correo == null
                ||
                correo.isBlank()
        ) {

            correo =
                    jwt.getClaimAsString(
                            "upn"
                    );
        }

        if (
                correo == null
                ||
                correo.isBlank()
        ) {

            correo =
                    jwt.getClaimAsString(
                            "email"
                    );
        }

        if (
                correo == null
                ||
                correo.isBlank()
        ) {

            throw new IllegalStateException(
                    "El token no contiene el correo del usuario."
            );
        }

        return correo.trim();
    }


    // =====================================================
    // HEADERS
    // =====================================================

    private HttpHeaders crearHeadersConToken(
            String authHeader) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        if (authHeader != null) {

            headers.set(
                    HttpHeaders.AUTHORIZATION,
                    authHeader
            );
        }

        return headers;
    }
}