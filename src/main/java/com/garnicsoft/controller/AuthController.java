package com.garnicsoft.controller;

import com.garnicsoft.dto.LoginByEmailAndPassRequest;
import com.garnicsoft.dto.LoginSuccessfulResponse;
import com.garnicsoft.dto.BasicRegisterRequest;
import com.garnicsoft.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService securityService) {
    this.authService = securityService;
  }

  @PostMapping(
      value = "/login",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<LoginSuccessfulResponse>> loginByEmailAndPass(
      @Valid @RequestBody Mono<LoginByEmailAndPassRequest> request) {

    return request
        .flatMap(authService::login)
        .map(ResponseEntity::ok)
        .switchIfEmpty(Mono.defer(() -> Mono.just(ResponseEntity.notFound().build())));
  }

  @PostMapping(
          value = "/basic-register",
          consumes = MediaType.APPLICATION_JSON_VALUE,
          produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<LoginSuccessfulResponse>> signUpBasic(
          @Valid @RequestBody Mono<BasicRegisterRequest> request) {

    return request
            .flatMap(authService::sigUpBasic)
            .map(ResponseEntity::ok)
            .switchIfEmpty(Mono.defer(() -> Mono.just(ResponseEntity.notFound().build())));
  }
}
