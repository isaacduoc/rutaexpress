
package com.duoc.ms_rutaexpress_rabbit_admin.controller;

import com.duoc.ms_rutaexpress_rabbit_admin.service.RabbitAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        RabbitAdminService servicio = mock(RabbitAdminService.class);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mvc = MockMvcBuilders.standaloneSetup(
                        new ColaAdminController(servicio),
                        new ExchangeAdminController(servicio),
                        new BindingAdminController(servicio)
                )
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void colaSinNombreDevuelve400() throws Exception {
        mvc.perform(post("/api/admin/colas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensaje",
                    containsString("El nombre es obligatorio")));
    }

    @Test
    void exchangeConTipoInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/admin/exchanges")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nombre": "ep2.admin.prueba",
                          "tipo": "invalido"
                        }
                        """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensaje",
                    containsString("El tipo debe ser direct o topic")));
    }

    @Test
    void bindingConRoutingKeyInvalidaDevuelve400() throws Exception {
        mvc.perform(post("/api/admin/bindings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "cola": "ep2.admin.cola",
                          "exchange": "ep2.admin.exchange",
                          "routingKey": "incorrecta"
                        }
                        """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.mensaje",
                    containsString("La routing key debe comenzar con ep2.")));
    }

    @Test
    void jsonInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/admin/colas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("JSON invalido"));
    }
}
