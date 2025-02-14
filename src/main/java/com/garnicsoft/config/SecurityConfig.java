package com.garnicsoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable) // Deshabilitar CSRF
        .authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers("/auth/login", "/auth/basic-register", "/actuator/**")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Deshabilitar autenticación básica
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // Deshabilitar formularios
        .exceptionHandling(
            exceptions ->
                exceptions.authenticationEntryPoint(
                    (swe, e) ->
                        Mono.fromRunnable(
                            () ->
                                swe.getResponse()
                                    .setStatusCode(
                                        org.springframework.http.HttpStatus.UNAUTHORIZED))))
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
