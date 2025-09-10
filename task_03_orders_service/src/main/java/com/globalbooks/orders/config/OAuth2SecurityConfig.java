package com.globalbooks.orders.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests(authz -> authz
                .antMatchers("/api/v1/orders/health").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/v1/orders/**").permitAll()  // Allow all order endpoints for testing
                .anyRequest().authenticated()
            );

        // Temporarily disable OAuth2 for testing
        // .oauth2ResourceServer(oauth2 -> oauth2
        //     .jwt(jwt -> jwt
        //         .decoder(jwtDecoder())
        //         .jwtAuthenticationConverter(jwtAuthenticationConverter())
        //     )
        // );

        // Allow H2 console in development
        http.headers().frameOptions().disable();

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract authorities from JWT claims
            return jwt.getClaimAsStringList("authorities").stream()
                .map(authority -> new SimpleGrantedAuthority(authority))
                .collect(Collectors.toList());
        });
        return converter;
    }
}