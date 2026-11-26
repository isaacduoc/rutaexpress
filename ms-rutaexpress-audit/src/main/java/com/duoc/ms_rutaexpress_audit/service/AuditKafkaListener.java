package com.duoc.ms_rutaexpress_audit.service;

import com.duoc.ms_rutaexpress_audit.entity.AuditEvent;
import com.duoc.ms_rutaexpress_audit.event.ShipmentEvent;
import com.duoc.ms_rutaexpress_audit.repository.AuditEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuditKafkaListener {

    private final AuditEventRepository auditEventRepository;
    private final AuditKafkaProducer auditKafkaProducer;

    public AuditKafkaListener(AuditEventRepository auditEventRepository,
                              AuditKafkaProducer auditKafkaProducer) {
        this.auditEventRepository = auditEventRepository;
        this.auditKafkaProducer = auditKafkaProducer;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.shipments-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void recibirEvento(ShipmentEvent evento) {

        AuditEvent auditEvent = new AuditEvent(
                evento.getId(),
                evento.getCodigoSeguimiento(),
                evento.getEstado(),
                evento.getFecha(),
                evento.getCorreoDestinatario(),
                evento.getServicio()
        );

        auditEventRepository.save(auditEvent);
        auditKafkaProducer.publicar(evento);

        System.out.println(
                "Evento recibido por Audit: "
                        + evento.getCodigoSeguimiento()
                        + " - Estado: "
                        + evento.getEstado()
                        + " - Servicio: "
                        + evento.getServicio()
        );
    }
}