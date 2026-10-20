
package com.duoc.ms_rutaexpress_bff.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping({"/bff/v1/admin", "/api/admin"})
public class RabbitAdminBffController {

    private final RestTemplate restTemplate;

    @Value("${rabbit.admin.service.url}")
    private String rabbitAdminUrl;

    @Value("${rabbit.admin.service.user}")
    private String usuarioInterno;

    @Value("${rabbit.admin.service.password}")
    private String claveInterna;

    public RabbitAdminBffController(
            RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // CONSULTAR UNA COLA DE RABBITMQ
    @GetMapping("/colas/{nombre}")
    public ResponseEntity<String> consultarCola(
            @PathVariable String nombre) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(
                usuarioInterno,
                claveInterna);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        URI url = UriComponentsBuilder
                .fromUriString(rabbitAdminUrl)
                .pathSegment("colas", nombre)
                .build()
                .encode()
                .toUri();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class);
    }

    // CREAR UNA COLA DE RABBITMQ
    @PostMapping("/colas")
    public ResponseEntity<String> crearCola(
            @RequestBody Map<String, String> request) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(
                usuarioInterno,
                claveInterna);

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        URI url = UriComponentsBuilder
                .fromUriString(rabbitAdminUrl)
                .pathSegment("colas")
                .build()
                .toUri();

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);
    }

    // ELIMINAR UNA COLA DE RABBITMQ
    @DeleteMapping("/colas/{nombre}")
    public ResponseEntity<Void> eliminarCola(
            @PathVariable String nombre) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(
                usuarioInterno,
                claveInterna);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        URI url = UriComponentsBuilder
                .fromUriString(rabbitAdminUrl)
                .pathSegment("colas", nombre)
                .build()
                .encode()
                .toUri();

        return restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                Void.class);
    }

    // CREAR UN EXCHANGE DE RABBITMQ
    @PostMapping("/exchanges")
    public ResponseEntity<String> crearExchange(
            @RequestBody Map<String, String> request) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(
                usuarioInterno,
                claveInterna);

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        URI url = UriComponentsBuilder
                .fromUriString(rabbitAdminUrl)
                .pathSegment("exchanges")
                .build()
                .toUri();

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);
    }

    // ELIMINAR UN EXCHANGE DE RABBITMQ
    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<Void> eliminarExchange(
            @PathVariable String nombre) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(
                usuarioInterno,
                claveInterna);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        URI url = UriComponentsBuilder
                .fromUriString(rabbitAdminUrl)
                .pathSegment("exchanges", nombre)
                .build()
                .encode()
                .toUri();

        return restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                Void.class);
    }

    // CREAR UN BINDING DE RABBITMQ
    @PostMapping("/bindings")
    public ResponseEntity<String> crearBinding(
            @RequestBody Map<String, String> request) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(
                usuarioInterno,
                claveInterna);

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        URI url = UriComponentsBuilder
                .fromUriString(rabbitAdminUrl)
                .pathSegment("bindings")
                .build()
                .toUri();

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);
    }

    // ELIMINAR UN BINDING DE RABBITMQ
    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(
            @RequestBody Map<String, String> request) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(
                usuarioInterno,
                claveInterna);

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        URI url = UriComponentsBuilder
                .fromUriString(rabbitAdminUrl)
                .pathSegment("bindings")
                .build()
                .toUri();

        return restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                Void.class);
    }

}
