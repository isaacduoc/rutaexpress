package com.duoc.ms_rutaexpress_audit.service;

import com.duoc.ms_rutaexpress_audit.event.ShipmentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuditKafkaProducer {

    private final KafkaTemplate<String, ShipmentEvent> kafkaTemplate;

    @Value("${app.kafka.topic.audit-timeline}")
    private String topic;

    public AuditKafkaProducer(KafkaTemplate<String, ShipmentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(ShipmentEvent evento) {
        try {
            kafkaTemplate.send(topic, evento.getId().toString(), evento)
                    .get(10, TimeUnit.SECONDS);

            System.out.println("Evento publicado en audit.timeline: "
                    + evento.getCodigoSeguimiento());

        } catch (Exception e) {
            System.err.println("Error publicando en audit.timeline: "
                    + e.getMessage());
            throw new IllegalStateException(
                    "No se pudo publicar el evento de auditoria", e);
        }
    }
}
