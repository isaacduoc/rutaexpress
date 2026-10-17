
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
public class EtiquetaService {

    private static final Logger log =
            LoggerFactory.getLogger(EtiquetaService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path directorio;

    public EtiquetaService(
            @Value("${rutaexpress.etiquetas.directorio:/app/etiquetas}")
            String rutaDirectorio) {

        this.directorio = Path.of(rutaDirectorio);
    }

    public Path generarEtiqueta(String mensaje) {

        JsonNode datos;

        try {
            datos = objectMapper.readTree(mensaje);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "El comando Label no contiene JSON valido", e
            );
        }

        if (datos == null || !datos.isObject()) {
            throw new IllegalArgumentException(
                    "El comando Label debe ser un objeto JSON"
            );
        }

        long envioId = datos.path("envioId").asLong(-1);

        if (envioId <= 0) {
            throw new IllegalArgumentException(
                    "ID de envio invalido"
            );
        }

        String codigo = obligatorio(datos, "codigoSeguimiento");
        String destinatario = obligatorio(datos, "destinatario");
        String destino = obligatorio(datos, "direccionDestino");
        String servicio = obligatorio(datos, "servicio");

        String tipoPaquete = datos.path("tipoPaquete")
                .asText("No especificado");

        String peso = datos.path("peso").isNumber()
                ? datos.path("peso").asText() + " kg"
                : "No especificado";

        // Etiqueta imprimible de 100 x 150 mm
        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Etiqueta RutaExpress</title>
                    <style>
                        @page { size: 100mm 150mm; margin: 0; }
                        body {
                            font-family: Arial, sans-serif;
                            padding: 10mm;
                            box-sizing: border-box;
                        }
                        h1 { font-size: 24px; }
                        .codigo {
                            font-size: 21px;
                            font-weight: bold;
                            border: 2px solid black;
                            padding: 12px;
                            text-align: center;
                        }
                        p { font-size: 15px; }
                    </style>
                </head>
                <body>
                    <h1>RutaExpress</h1>
                    <h2>ETIQUETA DE ENVIO</h2>
                    <div class="codigo">%s</div>
                    <p><b>Destinatario:</b> %s</p>
                    <p><b>Destino:</b> %s</p>
                    <p><b>Servicio:</b> %s</p>
                    <p><b>Tipo de paquete:</b> %s</p>
                    <p><b>Peso:</b> %s</p>
                </body>
                </html>
                """.formatted(
                        escapar(codigo),
                        escapar(destinatario),
                        escapar(destino),
                        escapar(servicio),
                        escapar(tipoPaquete),
                        escapar(peso)
                );

        Path archivo = directorio.resolve(
                "etiqueta-envio-" + envioId + ".html"
        );

        try {
            Files.createDirectories(directorio);

            // No sobrescribir una etiqueta ya generada
            Files.writeString(
                    archivo,
                    html,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );

            log.info(
                    "[LABEL] Etiqueta creada para envio {} en {}",
                    envioId,
                    archivo
            );

        } catch (FileAlreadyExistsException e) {

            log.info(
                    "[LABEL] El envio {} ya tiene etiqueta",
                    envioId
            );

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "No fue posible guardar la etiqueta del envio "
                            + envioId,
                    e
            );
        }

        return archivo;
    }

    private String obligatorio(JsonNode datos, String campo) {

        String valor = datos.path(campo).asText("").trim();

        if (valor.isBlank()) {
            throw new IllegalArgumentException(
                    "Falta el campo obligatorio: " + campo
            );
        }

        return valor;
    }

    private String escapar(String texto) {

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
