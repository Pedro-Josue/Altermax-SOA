package com.altermax.auth.infrastructure;

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

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            SecurityErrorWriter errorWriter)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        authorization ->
                                authorization
                                        .requestMatchers(
                                                "/api/auth/login",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/v3/api-docs/**",
                                                "/h2-console/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/ford-vehicles/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/api/ford-vehicles/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PATCH, "/api/ford-vehicles/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/api/ford-vehicles/**")
                                        .hasRole("ADMIN")
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        exceptionHandling ->
                                exceptionHandling
                                        .authenticationEntryPoint(
                                                (request, response, exception) ->
                                                        errorWriter.write(
                                                                request,
                                                                response,
                                                                401,
                                                                "Unauthorized",
                                                                "UNAUTHORIZED",
                                                                "Autenticacao JWT ausente ou invalida."))
                                        .accessDeniedHandler(
                                                (request, response, exception) ->
                                                        errorWriter.write(
                                                                request,
                                                                response,
                                                                403,
                                                                "Forbidden",
                                                                "ACCESS_DENIED",
                                                                "Voce nao possui permissao para este recurso.")))
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
