package com.duoc.backend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
public class Envio implements Serializable {

    private static final long serialVersionUID = 1L;


    // =====================================================
    // IDENTIFICACIÓN
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigoSeguimiento;


    // =====================================================
    // DATOS DEL ENVÍO
    // =====================================================

    @Column(nullable = false)
    private String correoRemitente;

    @Column(nullable = false)
    private String nombreDestinatario;

    @Column(nullable = false)
    private String correoDestinatario;

    @Column(nullable = false)
    private String direccionOrigen;

    @Column(nullable = false)
    private String direccionDestino;

    @Column(nullable = false)
    private String servicio;


    // =====================================================
    // DATOS DEL PAQUETE
    // =====================================================

    /*
     * Ejemplos:
     * DOCUMENTO
     * PAQUETE_PEQUENO
     * PAQUETE_MEDIANO
     * PAQUETE_GRANDE
     */
    @Column(name = "tipo_paquete")
    private String tipoPaquete;


    /*
     * Dimensiones expresadas en centímetros.
     */
    @Column(name = "alto_cm")
    private Double alto;

    @Column(name = "ancho_cm")
    private Double ancho;

    @Column(name = "largo_cm")
    private Double largo;


    /*
     * Peso real informado por el Cliente,
     * expresado en kilogramos.
     */
    @Column(name = "peso_kg")
    private Double peso;


    /*
     * Valor declarado del contenido.
     */
    @Column(name = "valor_declarado")
    private Double valorDeclarado;


    /*
     * Indica si el contenido requiere
     * manipulación especial.
     */
    @Column(name = "fragil")
    private Boolean fragil = false;


    /*
     * Instrucciones adicionales:
     * "Llamar antes de entregar",
     * "No dejar en conserjería", etc.
     */
    @Column(
        name = "observaciones",
        length = 1000
    )
    private String observaciones;


    // =====================================================
    // COTIZACIÓN
    // =====================================================

    /*
     * Peso calculado usando dimensiones:
     *
     * alto × ancho × largo / 4000
     */
    @Column(name = "peso_volumetrico")
    private Double pesoVolumetrico;


    /*
     * Se cobra utilizando el mayor entre
     * peso real y peso volumétrico.
     */
    @Column(name = "peso_cobrable")
    private Double pesoCobrable;


    /*
     * Tarifa obtenida desde el
     * microservicio de Catálogo.
     */
    @Column(name = "tarifa_base")
    private Double tarifaBase;


    /*
     * Recargo generado por peso adicional.
     */
    @Column(name = "recargo_peso")
    private Double recargoPeso;


    /*
     * Recargo aplicado cuando
     * el envío es frágil.
     */
    @Column(name = "recargo_fragilidad")
    private Double recargoFragilidad;


    /*
     * Precio final calculado por Shipments.
     */
    @Column(name = "precio_estimado")
    private Double precioEstimado;


    // =====================================================
    // ESTADO
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEnvio estado;


    /*
     * Borrado lógico.
     *
     * false/null = visible en módulo Envíos
     * true       = quitado de la sección operativa
     */
    @Column(name = "archivado")
    private Boolean archivado = false;


    // =====================================================
    // FECHAS
    // =====================================================

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;


    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    @PrePersist
    protected void onCreate() {

        this.fechaCreacion =
                LocalDateTime.now();

        this.fechaActualizacion =
                LocalDateTime.now();

        if (this.estado == null) {

            this.estado =
                    EstadoEnvio.CREADO;
        }

        if (this.archivado == null) {

            this.archivado =
                    false;
        }

        if (this.fragil == null) {

            this.fragil =
                    false;
        }
    }


    @PreUpdate
    protected void onUpdate() {

        this.fechaActualizacion =
                LocalDateTime.now();
    }


    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }


    public String getCodigoSeguimiento() {
        return codigoSeguimiento;
    }

    public void setCodigoSeguimiento(
            String codigoSeguimiento) {

        this.codigoSeguimiento =
                codigoSeguimiento;
    }


    public String getCorreoRemitente() {
        return correoRemitente;
    }

    public void setCorreoRemitente(
            String correoRemitente) {

        this.correoRemitente =
                correoRemitente;
    }


    public String getNombreDestinatario() {
        return nombreDestinatario;
    }

    public void setNombreDestinatario(
            String nombreDestinatario) {

        this.nombreDestinatario =
                nombreDestinatario;
    }


    public String getCorreoDestinatario() {
        return correoDestinatario;
    }

    public void setCorreoDestinatario(
            String correoDestinatario) {

        this.correoDestinatario =
                correoDestinatario;
    }


    public String getDireccionOrigen() {
        return direccionOrigen;
    }

    public void setDireccionOrigen(
            String direccionOrigen) {

        this.direccionOrigen =
                direccionOrigen;
    }


    public String getDireccionDestino() {
        return direccionDestino;
    }

    public void setDireccionDestino(
            String direccionDestino) {

        this.direccionDestino =
                direccionDestino;
    }


    public String getServicio() {
        return servicio;
    }

    public void setServicio(
            String servicio) {

        this.servicio =
                servicio;
    }


    // =====================================================
    // PAQUETE
    // =====================================================

    public String getTipoPaquete() {
        return tipoPaquete;
    }

    public void setTipoPaquete(
            String tipoPaquete) {

        this.tipoPaquete =
                tipoPaquete;
    }


    public Double getAlto() {
        return alto;
    }

    public void setAlto(
            Double alto) {

        this.alto = alto;
    }


    public Double getAncho() {
        return ancho;
    }

    public void setAncho(
            Double ancho) {

        this.ancho = ancho;
    }


    public Double getLargo() {
        return largo;
    }

    public void setLargo(
            Double largo) {

        this.largo = largo;
    }


    public Double getPeso() {
        return peso;
    }

    public void setPeso(
            Double peso) {

        this.peso = peso;
    }


    public Double getValorDeclarado() {
        return valorDeclarado;
    }

    public void setValorDeclarado(
            Double valorDeclarado) {

        this.valorDeclarado =
                valorDeclarado;
    }


    public Boolean getFragil() {
        return fragil;
    }

    public void setFragil(
            Boolean fragil) {

        this.fragil = fragil;
    }


    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones) {

        this.observaciones =
                observaciones;
    }


    // =====================================================
    // COTIZACIÓN
    // =====================================================

    public Double getPesoVolumetrico() {
        return pesoVolumetrico;
    }

    public void setPesoVolumetrico(
            Double pesoVolumetrico) {

        this.pesoVolumetrico =
                pesoVolumetrico;
    }


    public Double getPesoCobrable() {
        return pesoCobrable;
    }

    public void setPesoCobrable(
            Double pesoCobrable) {

        this.pesoCobrable =
                pesoCobrable;
    }


    public Double getTarifaBase() {
        return tarifaBase;
    }

    public void setTarifaBase(
            Double tarifaBase) {

        this.tarifaBase =
                tarifaBase;
    }


    public Double getRecargoPeso() {
        return recargoPeso;
    }

    public void setRecargoPeso(
            Double recargoPeso) {

        this.recargoPeso =
                recargoPeso;
    }


    public Double getRecargoFragilidad() {
        return recargoFragilidad;
    }

    public void setRecargoFragilidad(
            Double recargoFragilidad) {

        this.recargoFragilidad =
                recargoFragilidad;
    }


    public Double getPrecioEstimado() {
        return precioEstimado;
    }

    public void setPrecioEstimado(
            Double precioEstimado) {

        this.precioEstimado =
                precioEstimado;
    }


    // =====================================================
    // ESTADO
    // =====================================================

    public EstadoEnvio getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoEnvio estado) {

        this.estado = estado;
    }


    public Boolean getArchivado() {
        return archivado;
    }

    public void setArchivado(
            Boolean archivado) {

        this.archivado = archivado;
    }


    // =====================================================
    // FECHAS
    // =====================================================

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}