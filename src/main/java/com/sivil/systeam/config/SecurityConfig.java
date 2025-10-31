package com.sivil.systeam.config;

import com.sivil.systeam.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize y @PostAuthorize
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Permitir acceso libre a login, registro y recursos estáticos
                        .requestMatchers("/login", "/registro", "/css/**", "/js/**", "/images/**").permitAll()
                        // Solo admin o vendedor pueden acceder a las rutas de inactivación de ventas
                        .requestMatchers("/ventas/inactivar/**").hasAnyRole("ADMIN", "VENDEDOR")
                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Página de login personalizada
                        .loginProcessingUrl("/login") // URL que procesa el login
                        .defaultSuccessUrl("/", true) // Redirigir al home tras login exitoso
                        .failureHandler((request, response, exception) -> {
                            // Manejar diferentes tipos de errores de autenticación
                            String errorParam = "credenciales";
                            
                            // Verificar el tipo de excepción o su causa
                            if (exception instanceof DisabledException) {
                                errorParam = "inactivo";
                            } else if (exception.getCause() instanceof DisabledException) {
                                errorParam = "inactivo";
                            } else if (exception.getMessage() != null && 
                                      exception.getMessage().contains("inactivada")) {
                                errorParam = "inactivo";
                            }
                            
                            response.sendRedirect("/login?error=" + errorParam);
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1) // Máx una sesión por usuario
                        .maxSessionsPreventsLogin(false) // Si inicia en otro lado, invalida la anterior
                )
                // Registrar el provider personalizado
                .authenticationProvider(daoAuthenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Solo para pruebas (sin encriptación)
        // ⚠️ En producción usa BCryptPasswordEncoder()
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        return new ProviderManager(daoAuthenticationProvider());
    }
}
