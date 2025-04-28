package com.mulaflow.mulaflow.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulaflow.mulaflow.dto.error.ErrorResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    HttpMethod.POST, "/api/v1/auth/**"
                ).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(handling -> handling
                // Custom JSON response for 401
                .authenticationEntryPoint((request, response, authException) -> {
                    ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        authException.getMessage(),
                        request.getRequestURI()
                    );
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                })
                // Custom JSON response for 403
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.FORBIDDEN,
                        HttpStatus.FORBIDDEN.getReasonPhrase(),
                        accessDeniedException.getMessage(),
                        request.getRequestURI()
                    );
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                })
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}