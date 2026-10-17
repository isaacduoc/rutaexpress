
package com.duoc.ms_rutaexpress_catalog.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "TICKETS_BODEGA",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_TICKET_BODEGA_ENVIO",
        columnNames = "ENVIO_ID"
    )
)
public class TicketBodega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ENVIO_ID", nullable = false)
    private Long envioId;

    @Column(
        name = "CODIGO_SEGUIMIENTO",
        nullable = false,
        length = 120
    )
    private String codigoSeguimiento;

    @Column(name = "SERVICIO", nullable = false)
    private String servicio;

    @Column(name = "DIRECCION_DESTINO", nullable = false)
    private String direccionDestino;

    @Column(name = "ESTADO_TICKET", nullable = false)
    private String estadoTicket = "PENDIENTE";

    @Column(
        name = "FECHA_CREACION",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaCreacion;

    public TicketBodega() {
    }

    public TicketBodega(
            Long envioId,
            String codigoSeguimiento,
            String servicio,
            String direccionDestino) {

        this.envioId = envioId;
        this.codigoSeguimiento = codigoSeguimiento;
        this.servicio = servicio;
        this.direccionDestino = direccionDestino;
        this.estadoTicket = "PENDIENTE";
    }

    @PrePersist
    public void antesDeGuardar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getEnvioId() {
        return envioId;
    }

    public String getCodigoSeguimiento() {
        return codigoSeguimiento;
    }

    public String getServicio() {
        return servicio;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public String getEstadoTicket() {
        return estadoTicket;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setEstadoTicket(String estadoTicket) {
        this.estadoTicket = estadoTicket;
    }
}
