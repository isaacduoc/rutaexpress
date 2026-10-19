package com.duoc.ms_rutaexpress_bff;

import java.util.List;

import com.duoc.ms_rutaexpress_bff.config.SecurityConfig;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

@SpringBootTest
@AutoConfigureMockMvc
class MsRutaexpressBffApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	@WithMockUser(roles = "Cliente")
	void clienteNoPuedeAdministrarRabbitMQ() throws Exception {

		mockMvc.perform(
				get("/bff/v1/admin/colas/ep2.admin.prueba"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "Despachador")
	void despachadorNoPuedeAdministrarRabbitMQ()
			throws Exception {

		mockMvc.perform(
				get("/bff/v1/admin/colas/ep2.admin.prueba"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "Admin")
	void adminPuedeConsultarCola() throws Exception {

		when(restTemplate.exchange(
				any(URI.class),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class))).thenReturn(
						ResponseEntity.ok(
								"{\"nombre\":\"ep2.admin.prueba\"}"));

		mockMvc.perform(
				get("/bff/v1/admin/colas/ep2.admin.prueba"))
				.andExpect(status().isOk());
	}

	@Test
	void jwtConvierteRolAdminCorrectamente() {

		Jwt jwt = Jwt.withTokenValue("token-de-prueba")
				.header("alg", "none")
				.claim("roles", List.of("Admin"))
				.build();

		SecurityConfig securityConfig = new SecurityConfig();

		AbstractAuthenticationToken authentication = securityConfig.azureJwtAuthenticationConverter()
				.convert(jwt);

		assertNotNull(authentication);

		assertTrue(
				authentication.getAuthorities()
						.stream()
						.anyMatch(authority -> authority.getAuthority()
								.equals("ROLE_Admin")));
	}

}
