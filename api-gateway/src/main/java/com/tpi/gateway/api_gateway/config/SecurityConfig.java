package com.tpi.gateway.api_gateway.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // Roles que Keycloak incluye por defecto y que no queremos mapear como GrantedAuthorities
    private static final Set<String> ROLES_POR_DEFECTO = Set.of("OFFLINE_ACCESS", "UMA_AUTHORIZATION");

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // Permitir acceso a la documentación de OpenAPI
                .pathMatchers("/webjars/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                // Permitir acceso sin autenticación a los endpoints de Keycloak para obtener certificados
                .pathMatchers("/realms/**").permitAll()
                // Asegurar que todas las demás rutas requieran autenticación
                .anyExchange().authenticated() 
            )
            // Configurar OAuth 2.0 Resource Server para JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // Uso de ReactiveJwtAuthenticationConverterAdapter para el mapeo de roles
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()) 
                )
            );
        return http.build();
    }

    /**
     * Define el conversor reactivo para transformar un JWT en un objeto de autenticación.
     */
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        // Asignar el conversor de roles personalizado
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtGrantedAuthoritiesConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    /**
     * Configura el ReactiveJwtDecoder con validación para múltiples issuers.
     * **JWK Set URI usa keycloak:8080 (red Docker), pero valida issuer localhost:9090 (testing externo).**
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // IMPORTANTE: El Gateway corre DENTRO de Docker, debe usar el nombre de servicio para obtener las claves
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
            .withJwkSetUri("http://keycloak:8080/realms/tpi-realm/protocol/openid-connect/certs")
            .build();
        
        // Validar tokens emitidos para testing externo (puerto 9090 expuesto)
        OAuth2TokenValidator<Jwt> withLocalhostIssuer = JwtValidators.createDefaultWithIssuer("http://localhost:9090/realms/tpi-realm"); 
        
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            withLocalhostIssuer,
            new JwtTimestampValidator()
        );
        
        decoder.setJwtValidator(validator);
        return decoder;
    }

    /**
     * Clase interna para extraer los roles de Keycloak (del claim 'realm_access') 
     * y mapearlos como GrantedAuthorities de Spring Security (ej: ROLE_ADMIN).
     */
    private static class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Object realmAccess = jwt.getClaim("realm_access");
            if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
                return Collections.emptyList();
            }

            Object rolesObj = realmAccessMap.get("roles");
            if (!(rolesObj instanceof List<?> roles)) {
                return Collections.emptyList();
            }

            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .map(String::toUpperCase)
                    .filter(role -> !ROLES_POR_DEFECTO.contains(role))
                    .filter(role -> !role.startsWith("DEFAULT-"))
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
    }
}