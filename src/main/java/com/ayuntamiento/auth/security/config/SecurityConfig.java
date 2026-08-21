package com.ayuntamiento.auth.security.config;

import com.ayuntamiento.auth.security.jwt.AuthJwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.ayuntamiento.security_lib.jwt.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
//importaciones cors

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthJwtAuthenticationFilter jwtAuthenticationFilter;

    // Inyectamos a nuestro Policía
    public SecurityConfig(AuthJwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Gestor de Autenticación
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> apagarFiltroDeLaLibreria(JwtAuthenticationFilter filtroLibreria) {
        
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(filtroLibreria);
        
        // ¡LA MAGIA OCURRE AQUÍ! 
        // Le decimos a Spring Boot que NO ejecute este filtro en este microservicio
        registrationBean.setEnabled(false); 
        
        return registrationBean;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http.csrf(csrf -> csrf.disable()) // Desactivamos CSRF porque se usa tokens
            .authorizeHttpRequests(auth -> auth
                // RUTAS PÚBLICAS
                .requestMatchers(HttpMethod.POST, "/api/auth/registrar").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/puestos/**").permitAll()
                // RUTAS PRIVADAS
                .anyRequest().authenticated() 
            )
            //sprign no guarda sesiones ne la memoria
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}