package com.sivil.systeam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Permitir acceso público a recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Permitir acceso público a las vistas
                .requestMatchers("/", "/libros/**", "libro/lista").permitAll()
                // Permitir acceso público a la API (por ahora para desarrollo)
                .requestMatchers("/api/**").permitAll()
                // Cualquier otra petición requiere autenticación
                //.anyRequest().authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable); // Deshabilitar CSRF para la API

        return http.build();
    }
}