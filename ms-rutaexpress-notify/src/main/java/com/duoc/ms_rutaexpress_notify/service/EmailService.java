
package com.duoc.ms_rutaexpress_notify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path directorio;

    public EmailService(
            @Value("${rutaexpress.emails.directorio:/app/emails}")
            String rutaDirectorio) {

        this.directorio = Path.of(rutaDirectorio);
    }

    public Path prepararEmail(String mensaje) {

        JsonNode datos;

        try {
            datos = objectMapper.readTree(mensaje);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "JSON de Email invalido", e
            );
        }

        if (datos == null || !datos.isObject()) {
            throw new IllegalArgumentException(
                    "El mensaje Email debe ser un objeto JSON"
            );
        }

        long envioId = datos.path("id").asLong(-1);

        if (envioId <= 0) {
            throw new IllegalArgumentException(
                    "ID de envio invalido"
            );
        }

        String estado = obligatorio(datos, "estado");
        String destinatario = obligatorio(datos, "destinatario");
        String correo = obligatorio(datos, "correoDestinatario");
        String codigo = obligatorio(datos, "codigoSeguimiento");

        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Correo destinatario invalido"
            );
        }

        if (!estado.matches("[A-Z_]+")) {
            throw new IllegalArgumentException(
                    "Estado de envio invalido"
            );
        }

        String contenido = """
                From: RutaExpress <no-reply@rutaexpress.local>
                To: %s
                Subject: RutaExpress - Estado de envio %s
                MIME-Version: 1.0
                Content-Type: text/plain; charset=UTF-8

                Hola %s,

                Tu envio %s ahora se encuentra en estado: %s.

                Gracias por utilizar RutaExpress.
                """.formatted(
                        correo,
                        codigo,
                        destinatario,
                        codigo,
                        estado
                );

        Path archivo = directorio.resolve(
                "email-envio-" + envioId + "-" + estado + ".eml"
        );

        try {
            Files.createDirectories(directorio);

            Files.writeString(
                    archivo,
                    contenido,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );

            log.info(
                    "[EMAIL] Notificacion preparada para envio {}",
                    envioId
            );

        } catch (FileAlreadyExistsException e) {

            log.info(
                    "[EMAIL] Notificacion ya preparada para envio {} y estado {}",
                    envioId, estado
            );

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "No fue posible guardar el correo del envio "
                            + envioId,
                    e
            );
        }

        return archivo;
    }

    private String obligatorio(JsonNode datos, String campo) {

        String valor = datos.path(campo).asText("").trim();

        if (valor.isBlank()
                || valor.contains("\r")
                || valor.contains("\n")) {

            throw new IllegalArgumentException(
                    "Campo obligatorio o invalido: " + campo
            );
        }

        return valor;
    }
}
