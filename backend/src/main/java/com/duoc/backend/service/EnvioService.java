package com.duoc.backend.service;



import com.duoc.backend.dto.CrearEnvioRequest;

import com.duoc.backend.entity.Envio;

import com.duoc.backend.entity.EstadoEnvio;

import com.duoc.backend.repository.EnvioRepository;

import com.duoc.backend.event.KafkaProducer;

import com.duoc.backend.event.ShipmentEvent;



import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpMethod;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;



import java.math.BigDecimal;

import java.math.RoundingMode;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Map;

import java.util.UUID;



@Service

public class EnvioService {



    private final EnvioRepository envioRepository;

    private final NotificationPublisher notificationPublisher;

    private final RestTemplate restTemplate;

    private final KafkaProducer kafkaProducer;



    private static final String CATALOG_URL =

            "http://localhost:8083/api/catalog";



    /*

     * Factor volumétrico utilizado

     * para esta implementación académica.

     */

    private static final double FACTOR_VOLUMETRICO =

            4000.0;



    /*

     * Primer kilogramo incluido

     * en la tarifa base.

     */

    private static final double PESO_INCLUIDO =

            1.0;



    /*

     * Recargo por cada kilogramo

     * cobrable sobre el primero.

     */

    private static final double PRECIO_KILO_EXTRA =

            800.0;



    /*

     * Recargo de 10% de la tarifa base

     * si el paquete es frágil.

     */

    private static final double PORCENTAJE_FRAGIL =

            0.10;





    public EnvioService(

            EnvioRepository envioRepository,

            NotificationPublisher notificationPublisher,

            RestTemplate restTemplate,

            KafkaProducer kafkaProducer) {



        this.envioRepository =

                envioRepository;



        this.notificationPublisher =

                notificationPublisher;



        this.restTemplate =

                restTemplate;



        this.kafkaProducer =

                kafkaProducer;

    }





    // =====================================================

    // CREAR ENVÍO

    // =====================================================



    public Envio crearEnvio(

            CrearEnvioRequest datos) {



        /*

         * Primero validamos toda la

         * información recibida.

         */

        validarDatosEnvio(datos);



        /*

         * Consultamos el servicio directamente

         * en Catálogo.

         *

         * No confiamos en una tarifa enviada

         * por Angular.

         */

        Map<String, Object> servicioCatalogo =

                obtenerServicioPorNombre(

                        datos.getServicio()

                );



        double tarifaBase =

                obtenerTarifaBase(

                        servicioCatalogo

                );





        // =================================================

        // CÁLCULO DEL PAQUETE

        // =================================================



        boolean esDocumento =
                "DOCUMENTO".equalsIgnoreCase(
                        datos.getTipoPaquete()
                );


        double pesoVolumetrico;
        double pesoCobrable;
        double recargoPeso;
        double recargoFragilidad;


        if (esDocumento) {

            /*
             * Los documentos usan directamente
             * la tarifa base del servicio.
             */
            pesoVolumetrico = 0.0;
            pesoCobrable = 0.0;
            recargoPeso = 0.0;
            recargoFragilidad = 0.0;

        } else {

            pesoVolumetrico =
                    calcularPesoVolumetrico(
                            datos.getAlto(),
                            datos.getAncho(),
                            datos.getLargo()
                    );

            pesoCobrable =
                    Math.max(
                            datos.getPeso(),
                            pesoVolumetrico
                    );

            recargoPeso =
                    calcularRecargoPeso(
                            pesoCobrable
                    );

            recargoFragilidad =
                    calcularRecargoFragilidad(
                            tarifaBase,
                            Boolean.TRUE.equals(
                                    datos.getFragil()
                            )
                    );
        }


        double precioEstimado =
                tarifaBase
                        + recargoPeso
                        + recargoFragilidad;


        /*

         * Redondeamos los valores antes

         * de almacenarlos.

         */

        tarifaBase =

                redondear(

                        tarifaBase

                );



        pesoVolumetrico =

                redondear(

                        pesoVolumetrico

                );



        pesoCobrable =

                redondear(

                        pesoCobrable

                );



        recargoPeso =

                redondear(

                        recargoPeso

                );



        recargoFragilidad =

                redondear(

                        recargoFragilidad

                );



        precioEstimado =

                redondear(

                        precioEstimado

                );





        // =================================================

        // CREAR ENTIDAD

        // =================================================



        Envio envio =

                new Envio();



        envio.setCodigoSeguimiento(

                "REX-" +

                UUID.randomUUID()

                        .toString()

                        .substring(0, 8)

                        .toUpperCase()

        );





        // =================================================

        // DATOS GENERALES

        // =================================================



        envio.setCorreoRemitente(

                datos.getCorreoRemitente()

                        .trim()

        );



        envio.setNombreDestinatario(

                datos.getNombreDestinatario()

                        .trim()

        );



        envio.setCorreoDestinatario(

                datos.getCorreoDestinatario()

                        .trim()

        );



        envio.setDireccionOrigen(

                datos.getDireccionOrigen()

                        .trim()

        );



        envio.setDireccionDestino(

                datos.getDireccionDestino()

                        .trim()

        );



        envio.setServicio(

                datos.getServicio()

                        .trim()

        );





        // =================================================

        // DATOS DEL PAQUETE

        // =================================================



        envio.setTipoPaquete(

                datos.getTipoPaquete()

                        .trim()

        );



        if (esDocumento) {

            envio.setAlto(null);
            envio.setAncho(null);
            envio.setLargo(null);
            envio.setPeso(null);
            envio.setFragil(false);

        } else {

            envio.setAlto(
                    datos.getAlto()
            );

            envio.setAncho(
                    datos.getAncho()
            );

            envio.setLargo(
                    datos.getLargo()
            );

            envio.setPeso(
                    datos.getPeso()
            );

            envio.setFragil(
                    Boolean.TRUE.equals(
                            datos.getFragil()
                    )
            );
        }


        envio.setValorDeclarado(
                datos.getValorDeclarado()
        );


        if (

                datos.getObservaciones() != null

                &&

                !datos.getObservaciones()

                        .isBlank()

        ) {



            envio.setObservaciones(

                    datos.getObservaciones()

                            .trim()

            );



        } else {



            envio.setObservaciones(

                    null

            );

        }





        // =================================================

        // COTIZACIÓN

        // =================================================



        envio.setTarifaBase(

                tarifaBase

        );



        envio.setPesoVolumetrico(

                pesoVolumetrico

        );



        envio.setPesoCobrable(

                pesoCobrable

        );



        envio.setRecargoPeso(

                recargoPeso

        );



        envio.setRecargoFragilidad(

                recargoFragilidad

        );



        envio.setPrecioEstimado(

                precioEstimado

        );





        // =================================================

        // ESTADO

        // =================================================



        envio.setEstado(

                EstadoEnvio.CREADO

        );



        envio.setArchivado(

                false

        );





        // =================================================

        // GUARDAR

        // =================================================



        Envio envioGuardado =

                envioRepository.save(

                        envio

                );





        // =================================================

        // KAFKA

        // =================================================



        ShipmentEvent evento =

                new ShipmentEvent(

                        envioGuardado.getId(),

                        envioGuardado.getCodigoSeguimiento(),

                        envioGuardado.getEstado(),

                        LocalDateTime.now(),

                        envioGuardado.getCorreoDestinatario(),

                        envioGuardado.getServicio()

                );



        kafkaProducer.enviarEvento(

                evento

        );





        System.out.println(

                "Envío creado: "

                + envioGuardado.getCodigoSeguimiento()

                + " | Peso real: "

                + envioGuardado.getPeso()

                + " kg"

                + " | Peso volumétrico: "

                + envioGuardado.getPesoVolumetrico()

                + " kg"

                + " | Peso cobrable: "

                + envioGuardado.getPesoCobrable()

                + " kg"

                + " | Precio estimado: $"

                + envioGuardado.getPrecioEstimado()

        );





        return envioGuardado;

    }





    // =====================================================

    // OBTENER POR ID

    // =====================================================



    public Envio obtenerPorId(

            Long id) {



        return envioRepository

                .findById(id)

                .orElseThrow(() ->

                        new RuntimeException(

                                "Envío no encontrado con ID: "

                                + id

                        )

                );

    }





    // =====================================================

    // LISTADO OPERATIVO

    // =====================================================



    /*

     * correoRemitente == null:

     * Admin / Despachador → todos.

     *

     * correoRemitente != null:

     * Cliente → solamente propios.

     */

    public List<Envio> listarEnvios(

            EstadoEnvio estado,

            LocalDateTime from,

            LocalDateTime to,

            String correoRemitente) {



        List<Envio> envios;



        if (

                correoRemitente != null

                &&

                !correoRemitente.isBlank()

        ) {



            envios =

                    envioRepository

                            .findVisiblesByCorreoRemitente(

                                    correoRemitente.trim()

                            );



        } else {



            envios =

                    envioRepository

                            .findVisibles();

        }





        return envios

                .stream()



                .filter(envio ->

                        estado == null

                        ||

                        envio.getEstado() == estado

                )



                .filter(envio ->

                        from == null

                        ||

                        (

                                envio.getFechaCreacion() != null

                                &&

                                !envio.getFechaCreacion()

                                        .isBefore(from)

                        )

                )



                .filter(envio ->

                        to == null

                        ||

                        (

                                envio.getFechaCreacion() != null

                                &&

                                !envio.getFechaCreacion()

                                        .isAfter(to)

                        )

                )



                .toList();

    }





    // =====================================================

    // CAMBIAR ESTADO

    // =====================================================



    public Envio cambiarEstado(

            Long id,

            EstadoEnvio nuevoEstado) {



        Envio envio =

                obtenerPorId(id);





        if (

                Boolean.TRUE.equals(

                        envio.getArchivado()

                )

        ) {



            throw new IllegalArgumentException(

                    "El envío está archivado."

            );

        }





        EstadoEnvio estadoAnterior =

                envio.getEstado();





        /*

         * No permitir modificar un envío

         * que ya terminó.

         */

        if (

                estadoAnterior

                        == EstadoEnvio.ENTREGADO

                ||

                estadoAnterior

                        == EstadoEnvio.CANCELADO

        ) {



            throw new IllegalArgumentException(

                    "El envío ya se encuentra en un estado final."

            );

        }





        /*

         * CREADO no puede saltar

         * directamente a EN_RUTA.

         */

        if (

                nuevoEstado == EstadoEnvio.EN_RUTA

                &&

                estadoAnterior == EstadoEnvio.CREADO

        ) {



            throw new IllegalArgumentException(

                    "No se puede cambiar el estado a EN_RUTA "

                    +

                    "sin haber sido ACEPTADO previamente."

            );

        }





        /*

         * Descontar capacidad cuando pasa

         * por primera vez a ACEPTADO.

         */

        if (

                nuevoEstado == EstadoEnvio.ACEPTADO

                &&

                estadoAnterior != EstadoEnvio.ACEPTADO

        ) {



            descontarCapacidadServicio(

                    envio.getServicio()

            );

        }





        /*

         * Devolver capacidad al cancelar

         * después de haber consumido cupo.

         */

        if (

                nuevoEstado == EstadoEnvio.CANCELADO

                &&

                (

                        estadoAnterior == EstadoEnvio.ACEPTADO

                        ||

                        estadoAnterior == EstadoEnvio.EN_BODEGA

                        ||

                        estadoAnterior == EstadoEnvio.EN_RUTA

                )

        ) {



            devolverCapacidadServicio(

                    envio.getServicio()

            );

        }





        envio.setEstado(

                nuevoEstado

        );



        Envio envioActualizado =

                envioRepository.save(

                        envio

                );





        /*

         * RabbitMQ continúa temporalmente

         * desactivado como estaba.

         */



        // notificationPublisher

        //     .enviarNotificacionCambioEstado(

        //         envioActualizado

        //     );





        ShipmentEvent evento =

                new ShipmentEvent(

                        envioActualizado.getId(),

                        envioActualizado.getCodigoSeguimiento(),

                        envioActualizado.getEstado(),

                        LocalDateTime.now(),

                        envioActualizado.getCorreoDestinatario(),

                        envioActualizado.getServicio()

                );



        kafkaProducer.enviarEvento(

                evento

        );





        return envioActualizado;

    }





    // =====================================================

    // ARCHIVAR

    // =====================================================



    /*

     * "Eliminar" de la pantalla Envíos

     * sin borrar físicamente de la BD.

     *

     * Solamente se permite cuando ya está

     * ENTREGADO o CANCELADO.

     */

    public void archivarEnvio(

            Long id) {



        Envio envio =

                obtenerPorId(id);





        if (

                envio.getEstado()

                        != EstadoEnvio.ENTREGADO

                &&

                envio.getEstado()

                        != EstadoEnvio.CANCELADO

        ) {



            throw new IllegalArgumentException(

                    "Solo se pueden quitar de Envíos "

                    +

                    "los pedidos ENTREGADOS o CANCELADOS."

            );

        }





        envio.setArchivado(

                true

        );



        envioRepository.save(

                envio

        );

    }





    // =====================================================

    // VALIDACIONES DEL NUEVO ENVÍO

    // =====================================================



    private void validarDatosEnvio(

            CrearEnvioRequest datos) {



        if (datos == null) {



            throw new IllegalArgumentException(

                    "Los datos del envío son obligatorios."

            );

        }





        validarTexto(

                datos.getCorreoRemitente(),

                "El correo del remitente es obligatorio."

        );



        validarTexto(

                datos.getNombreDestinatario(),

                "El nombre del destinatario es obligatorio."

        );



        validarTexto(

                datos.getCorreoDestinatario(),

                "El correo del destinatario es obligatorio."

        );



        validarTexto(

                datos.getDireccionOrigen(),

                "La dirección de origen es obligatoria."

        );



        validarTexto(

                datos.getDireccionDestino(),

                "La dirección de destino es obligatoria."

        );



        validarTexto(

                datos.getServicio(),

                "Debes seleccionar un servicio."

        );



        validarTexto(

                datos.getTipoPaquete(),

                "Debes seleccionar un tipo de paquete."

        );





        boolean esDocumento =
                "DOCUMENTO".equalsIgnoreCase(
                        datos.getTipoPaquete()
                );


        /*
         * Peso y dimensiones solo son obligatorios
         * para paquetes físicos.
         */
        if (!esDocumento) {

            validarNumeroPositivo(
                    datos.getAlto(),
                    "El alto del paquete debe ser mayor que 0."
            );

            validarNumeroPositivo(
                    datos.getAncho(),
                    "El ancho del paquete debe ser mayor que 0."
            );

            validarNumeroPositivo(
                    datos.getLargo(),
                    "El largo del paquete debe ser mayor que 0."
            );

            validarNumeroPositivo(
                    datos.getPeso(),
                    "El peso del paquete debe ser mayor que 0."
            );
        }


        /*
         * El valor declarado es opcional.
         * Si se informa, no puede ser negativo.
         */
        if (
                datos.getValorDeclarado() != null
                &&
                datos.getValorDeclarado() < 0
        ) {

            throw new IllegalArgumentException(
                    "El valor declarado no puede ser negativo."
            );
        }


        if (

                datos.getObservaciones() != null

                &&

                datos.getObservaciones().length() > 1000

        ) {



            throw new IllegalArgumentException(

                    "Las observaciones no pueden superar los 1000 caracteres."

            );

        }

    }





    private void validarTexto(

            String valor,

            String mensaje) {



        if (

                valor == null

                ||

                valor.isBlank()

        ) {



            throw new IllegalArgumentException(

                    mensaje

            );

        }

    }





    private void validarNumeroPositivo(

            Double valor,

            String mensaje) {



        if (

                valor == null

                ||

                valor <= 0

                ||

                Double.isNaN(valor)

                ||

                Double.isInfinite(valor)

        ) {



            throw new IllegalArgumentException(

                    mensaje

            );

        }

    }





    // =====================================================

    // COTIZACIÓN

    // =====================================================



    private double calcularPesoVolumetrico(

            double alto,

            double ancho,

            double largo) {



        return (

                alto

                * ancho

                * largo

        ) / FACTOR_VOLUMETRICO;

    }





    private double calcularRecargoPeso(

            double pesoCobrable) {



        if (

                pesoCobrable

                        <= PESO_INCLUIDO

        ) {



            return 0.0;

        }





        return (

                pesoCobrable

                        - PESO_INCLUIDO

        ) * PRECIO_KILO_EXTRA;

    }





    private double calcularRecargoFragilidad(

            double tarifaBase,

            boolean fragil) {



        if (!fragil) {

            return 0.0;

        }



        return tarifaBase

                * PORCENTAJE_FRAGIL;

    }





    private double redondear(

            double valor) {



        return BigDecimal

                .valueOf(valor)

                .setScale(

                        2,

                        RoundingMode.HALF_UP

                )

                .doubleValue();

    }





    // =====================================================

    // CATÁLOGO

    // =====================================================



    /*

     * Obtiene el servicio completo

     * desde Catálogo.

     *

     * Así podemos reutilizar:

     * id

     * nombre

     * tarifaBase

     * capacidadDisponible

     */

    private Map<String, Object>

    obtenerServicioPorNombre(

            String nombreServicio) {



        ResponseEntity<

                List<Map<String, Object>>

        > response =



                restTemplate.exchange(



                        CATALOG_URL

                                + "/services",



                        HttpMethod.GET,



                        null,



                        new ParameterizedTypeReference<

                                List<Map<String, Object>>

                        >() {}

                );





        List<Map<String, Object>> servicios =

                response.getBody();





        if (

                servicios == null

                ||

                servicios.isEmpty()

        ) {



            throw new RuntimeException(

                    "El catálogo no contiene servicios."

            );

        }





        return servicios

                .stream()



                .filter(servicio -> {



                    Object nombre =

                            servicio.get(

                                    "nombre"

                            );



                    return nombre != null

                            &&

                            nombre.toString()

                                    .trim()

                                    .equalsIgnoreCase(

                                            nombreServicio.trim()

                                    );

                })



                .findFirst()



                .orElseThrow(() ->

                        new RuntimeException(

                                "No se encontró el servicio '"

                                +

                                nombreServicio

                                +

                                "' en el catálogo."

                        )

                );

    }





    /*

     * Obtiene la tarifa actual

     * directamente desde Catálogo.

     */

    private double obtenerTarifaBase(

            Map<String, Object> servicio) {



        Object tarifaObjeto =

                servicio.get(

                        "tarifaBase"

                );





        if (tarifaObjeto == null) {



            throw new RuntimeException(

                    "El servicio seleccionado no tiene tarifa base."

            );

        }





        try {



            return Double.parseDouble(

                    tarifaObjeto.toString()

            );



        } catch (

                NumberFormatException e

        ) {



            throw new RuntimeException(

                    "La tarifa base del servicio no es válida."

            );

        }

    }





    /*

     * Se mantiene para las operaciones

     * de capacidad.

     */

    private Long obtenerIdServicioPorNombre(

            String nombreServicio) {



        Map<String, Object> servicio =

                obtenerServicioPorNombre(

                        nombreServicio

                );





        Object idObjeto =

                servicio.get(

                        "id"

                );





        if (idObjeto == null) {



            throw new RuntimeException(

                    "El servicio encontrado no tiene ID."

            );

        }





        return Long.valueOf(

                idObjeto.toString()

        );

    }





    // =====================================================

    // CAPACIDAD DEL SERVICIO

    // =====================================================



    private void descontarCapacidadServicio(

            String nombreServicio) {



        try {



            Long servicioId =

                    obtenerIdServicioPorNombre(

                            nombreServicio

                    );





            String urlDescuento =

                    CATALOG_URL

                    + "/services/"

                    + servicioId

                    + "/descontar";





            restTemplate.put(

                    urlDescuento,

                    null

            );





            System.out.println(

                    "Capacidad descontada correctamente. "

                    +

                    "Servicio: "

                    +

                    nombreServicio

                    +

                    " - ID: "

                    +

                    servicioId

            );



        } catch (Exception e) {



            throw new RuntimeException(

                    "No se pudo descontar la capacidad "

                    +

                    "del servicio '"

                    +

                    nombreServicio

                    +

                    "': "

                    +

                    e.getMessage()

            );

        }

    }





    private void devolverCapacidadServicio(

            String nombreServicio) {



        try {



            Long servicioId =

                    obtenerIdServicioPorNombre(

                            nombreServicio

                    );





            String urlDevolucion =

                    CATALOG_URL

                    + "/services/"

                    + servicioId

                    + "/devolver";





            restTemplate.put(

                    urlDevolucion,

                    null

            );





            System.out.println(

                    "Capacidad devuelta correctamente. "

                    +

                    "Servicio: "

                    +

                    nombreServicio

                    +

                    " - ID: "

                    +

                    servicioId

            );



        } catch (Exception e) {



            throw new RuntimeException(

                    "No se pudo devolver la capacidad "

                    +

                    "del servicio '"

                    +

                    nombreServicio

                    +

                    "': "

                    +

                    e.getMessage()

            );

        }

    }

}