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

    public EnvioService(
            EnvioRepository envioRepository,
            NotificationPublisher notificationPublisher,
            RestTemplate restTemplate,
            KafkaProducer kafkaProducer) {

        this.envioRepository = envioRepository;
        this.notificationPublisher = notificationPublisher;
        this.restTemplate = restTemplate;
        this.kafkaProducer = kafkaProducer;
    }

    public Envio crearEnvio(CrearEnvioRequest datos) {

        Envio envio = new Envio();

        envio.setCodigoSeguimiento(
                "REX-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase()
        );

        envio.setCorreoRemitente(
                datos.getCorreoRemitente()
        );

        envio.setNombreDestinatario(
                datos.getNombreDestinatario()
        );

        envio.setCorreoDestinatario(
                datos.getCorreoDestinatario()
        );

        envio.setDireccionOrigen(
                datos.getDireccionOrigen()
        );

        envio.setDireccionDestino(
                datos.getDireccionDestino()
        );

        envio.setServicio(
                datos.getServicio()
        );

        envio.setEstado(
                EstadoEnvio.CREADO
        );

        Envio envioGuardado =
                envioRepository.save(envio);

        ShipmentEvent evento =
                new ShipmentEvent(
                        envioGuardado.getId(),
                        envioGuardado.getCodigoSeguimiento(),
                        envioGuardado.getEstado(),
                        LocalDateTime.now(),
                        envioGuardado.getCorreoDestinatario(),
                        envioGuardado.getServicio()
                );

        kafkaProducer.enviarEvento(evento);

        return envioGuardado;
    }

    public Envio obtenerPorId(Long id) {

        return envioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Envío no encontrado con ID: " + id
                        )
                );
    }

    public List<Envio> listarEnvios(
            EstadoEnvio estado) {

        if (estado != null) {
            return envioRepository
                    .findByEstado(estado);
        }

        return envioRepository.findAll();
    }

    public Envio cambiarEstado(
            Long id,
            EstadoEnvio nuevoEstado) {

        Envio envio =
                obtenerPorId(id);

        EstadoEnvio estadoAnterior =
                envio.getEstado();

        /*
         * Regla de negocio:
         * no permitir CREADO -> EN_RUTA directamente.
         */
        if (
                nuevoEstado == EstadoEnvio.EN_RUTA
                &&
                estadoAnterior == EstadoEnvio.CREADO
        ) {

            throw new IllegalArgumentException(
                    "No se puede cambiar el estado a EN_RUTA " +
                    "sin haber sido ACEPTADO previamente."
            );
        }

        /*
         * Descontar capacidad cuando el envío
         * pasa por primera vez a ACEPTADO.
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
         * Devolver capacidad cuando se cancela
         * un envío que ya había consumido un cupo.
         *
         * CREADO -> CANCELADO:
         * no devuelve capacidad.
         *
         * ACEPTADO / EN_BODEGA / EN_RUTA -> CANCELADO:
         * devuelve +1.
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
                envioRepository.save(envio);

        /*
         * RabbitMQ temporalmente desactivado.
         */

        // notificationPublisher
        //         .enviarNotificacionCambioEstado(
        //                 envioActualizado
        //         );

        ShipmentEvent evento =
                new ShipmentEvent(
                        envioActualizado.getId(),
                        envioActualizado.getCodigoSeguimiento(),
                        envioActualizado.getEstado(),
                        LocalDateTime.now(),
                        envioActualizado.getCorreoDestinatario(),
                        envioActualizado.getServicio()
                );

        kafkaProducer.enviarEvento(evento);

        return envioActualizado;
    }

    /*
     * Busca el servicio por nombre
     * y descuenta 1 de capacidad.
     */
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
                    + "Servicio: "
                    + nombreServicio
                    + " - ID: "
                    + servicioId
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo descontar la capacidad " +
                    "del servicio '" +
                    nombreServicio +
                    "': " +
                    e.getMessage()
            );
        }
    }

    /*
     * Busca el servicio por nombre
     * y devuelve 1 de capacidad.
     */
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
                    + "Servicio: "
                    + nombreServicio
                    + " - ID: "
                    + servicioId
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo devolver la capacidad " +
                    "del servicio '" +
                    nombreServicio +
                    "': " +
                    e.getMessage()
            );
        }
    }

    /*
     * Busca en el catálogo el ID
     * correspondiente al nombre del servicio.
     */
    private Long obtenerIdServicioPorNombre(
            String nombreServicio) {

        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(
                        CATALOG_URL + "/services",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<
                                List<Map<String, Object>>>() {}
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

        Map<String, Object> servicioEncontrado =
                servicios.stream()
                        .filter(servicio -> {

                            Object nombre =
                                    servicio.get("nombre");

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
                                        "No se encontró el servicio '" +
                                        nombreServicio +
                                        "' en el catálogo."
                                )
                        );

        Object idObjeto =
                servicioEncontrado.get("id");

        if (idObjeto == null) {

            throw new RuntimeException(
                    "El servicio encontrado no tiene ID."
            );
        }

        return Long.valueOf(
                idObjeto.toString()
        );
    }

    public List<Envio> listarEnvios(
            EstadoEnvio estado,
            LocalDateTime from,
            LocalDateTime to) {

        if (
                estado != null
                &&
                from != null
                &&
                to != null
        ) {

            return envioRepository
                    .findByEstadoAndFechaCreacionBetween(
                            estado,
                            from,
                            to
                    );
        }

        if (estado != null) {

            return envioRepository
                    .findByEstado(estado);
        }

        if (
                from != null
                &&
                to != null
        ) {

            return envioRepository
                    .findByFechaCreacionBetween(
                            from,
                            to
                    );
        }

        return envioRepository.findAll();
    }
}