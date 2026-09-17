package com.duoc.ms_rutaexpress_bff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bff/v1/audit")
public class AuditBffController {

    private final RestTemplate restTemplate;

    @Value("${audit.service.url}")
    private String auditServiceUrl;

    public AuditBffController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<String> getAuditEvents(
            @RequestHeader("Authorization") String authorization) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        auditServiceUrl,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response.getBody());
    }

    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<String> getAuditByShipment(
            @PathVariable Long shipmentId,
            @RequestHeader("Authorization") String authorization) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        auditServiceUrl + "/shipment/" + shipmentId,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response.getBody());
    }

    @GetMapping("/tracking/{codigoSeguimiento}")
    public ResponseEntity<String> getAuditByTracking(
            @PathVariable String codigoSeguimiento,
            @RequestHeader("Authorization") String authorization) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        auditServiceUrl + "/tracking/" + codigoSeguimiento,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response.getBody());
    }

    @GetMapping("/entregados-hoy")
    public ResponseEntity<String> getEntregadosHoy(
            @RequestHeader("Authorization") String authorization) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        auditServiceUrl + "/entregados-hoy",
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response.getBody());
    }
}