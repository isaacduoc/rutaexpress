package com.duoc.ms_rutaexpress_bff.dto;

public class CrearEnvioBffRequest {

    private String correoRemitente;
    private String nombreDestinatario;
    private String correoDestinatario;
    private String direccionOrigen;
    private String direccionDestino;
    private String servicio;

    // Datos del paquete
    private String tipoPaquete;

    private Double alto;
    private Double ancho;
    private Double largo;
    private Double peso;

    private Double valorDeclarado;

    private Boolean fragil;

    private String observaciones;


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
}