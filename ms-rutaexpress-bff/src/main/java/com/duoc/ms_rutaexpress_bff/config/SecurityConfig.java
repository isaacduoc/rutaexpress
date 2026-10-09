package com.duoc.ms_rutaexpress_bff.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors ->
                cors.configurationSource(
                    corsConfigurationSource()
                )
            )

            .authorizeHttpRequests(auth -> auth

                // =========================
                // CORS
                // =========================
                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                )
                .permitAll()

                // =========================
                // REPORTERIA
                // Solo Admin
                // =========================
                .requestMatchers(
                    "/bff/v1/report/**"
                )
                .hasRole("Admin")

                // =========================
                // AUDITORIA
                // Admin o Auditor
                // =========================
                .requestMatchers(
                    "/bff/v1/audit/**"
                )
                .hasAnyRole(
                    "Admin",
                    "Auditor"
                )

                // =========================
                // CATALOGO
                // =========================

                /*
                 * Cliente puede consultar
                 * servicios disponibles
                 * para crear un envío.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/bff/v1/catalog/services"
                )
                .hasAnyRole(
                    "Admin",
                    "Despachador",
                    "Cliente"
                )

                /*
                 * Otras consultas del catálogo:
                 * Admin o Despachador
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/bff/v1/catalog/**"
                )
                .hasAnyRole(
                    "Admin",
                    "Despachador"
                )

                /*
                 * Crear servicios:
                 * solo Admin
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/bff/v1/catalog/**"
                )
                .hasRole("Admin")

                /*
                 * Modificar tarifa/capacidad:
                 * solo Admin
                 */
                .requestMatchers(
                    HttpMethod.PUT,
                    "/bff/v1/catalog/**"
                )
                .hasRole("Admin")

                /*
                 * Eliminar del catálogo:
                 * solo Admin
                 */
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/bff/v1/catalog/**"
                )
                .hasRole("Admin")

                // =========================
                // ENVIOS
                // =========================

                /*
                 * Consultar envíos
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/bff/v1/shipments/**"
                )
                .hasAnyRole(
                    "Admin",
                    "Despachador",
                    "Cliente"
                )

                /*
                 * Crear envío
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/bff/v1/shipments/**"
                )
                .hasAnyRole(
                    "Admin",
                    "Despachador",
                    "Cliente"
                )

                /*
                 * Cambiar estado
                 */
                .requestMatchers(
                    HttpMethod.PUT,
                    "/bff/v1/shipments/**"
                )
                .hasAnyRole(
                    "Admin",
                    "Despachador"
                )

                /*
                 * Quitar/archivar un envío
                 * ya finalizado.
                 *
                 * Solo Admin o Despachador.
                 */
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/bff/v1/shipments/**"
                )
                .hasAnyRole(
                    "Admin",
                    "Despachador"
                )

                /*
                 * Cualquier otra ruta requiere
                 * un JWT válido.
                 */
                .anyRequest()
                .authenticated()
            )

            .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        azureJwtAuthenticationConverter()
                    )
                )
            );

        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken>
    azureJwtAuthenticationConverter() {

        return jwt -> {

            Collection<String> roles =
                jwt.getClaimAsStringList("roles");

            List<GrantedAuthority> authorities;

            if (roles == null) {

                authorities = List.of();

            } else {

                authorities = roles.stream()
                    .map(role ->
                        new SimpleGrantedAuthority(
                            "ROLE_" + role
                        )
                    )
                    .collect(Collectors.toList());
            }

            return new JwtAuthenticationToken(
                jwt,
                authorities
            );
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
            new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of(
                "http://localhost:4200",
                "https://rutaexpress-duoc.netlify.app"
            )
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );

        configuration.setExposedHeaders(
            List.of(
                "Authorization"
            )
        );

        configuration.setAllowCredentials(
            true
        );

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }
}