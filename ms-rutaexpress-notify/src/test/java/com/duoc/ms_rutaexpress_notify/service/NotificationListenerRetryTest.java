package com.duoc.ms_rutaexpress_notify.service;

import com.rabbitmq.client.Channel;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.mockito.Mockito.*;

class NotificationListenerRetryTest {

    @Test
    void emailReintentaTresVecesAntesDeNack() throws Exception {

        Channel channel = mock(Channel.class);
        EmailService emailService = mock(EmailService.class);
        EtiquetaService etiquetaService = mock(EtiquetaService.class);

        String mensaje = "{\"prueba\":\"error-temporal\"}";

        when(emailService.prepararEmail(mensaje))
                .thenThrow(new UncheckedIOException(
                        new IOException("Fallo temporal simulado")
                ));

        NotificationListener listener = new NotificationListener(
                etiquetaService,
                emailService
        );

        listener.consumirEmail(mensaje, channel, 7L);

        verify(emailService, times(3))
                .prepararEmail(mensaje);

        verify(channel).basicNack(7L, false, false);

        verify(channel, never())
                .basicAck(anyLong(), anyBoolean());
    }

    @Test
    void labelReintentaTresVecesAntesDeNack() throws Exception {

        Channel channel = mock(Channel.class);
        EmailService emailService = mock(EmailService.class);
        EtiquetaService etiquetaService = mock(EtiquetaService.class);

        String mensaje = "{\"prueba\":\"error-temporal\"}";

        when(etiquetaService.generarEtiqueta(mensaje))
                .thenThrow(new UncheckedIOException(
                        new IOException("Fallo temporal simulado")
                ));

        NotificationListener listener = new NotificationListener(
                etiquetaService,
                emailService
        );

        listener.consumirLabel(mensaje, channel, 8L);

        verify(etiquetaService, times(3))
                .generarEtiqueta(mensaje);

        verify(channel).basicNack(8L, false, false);

        verify(channel, never())
                .basicAck(anyLong(), anyBoolean());
    }
}
