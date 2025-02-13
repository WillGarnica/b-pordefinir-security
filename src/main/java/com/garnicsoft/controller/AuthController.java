package com.garnicsoft.controller;

import com.garnicsoft.dto.BasicRegisterRequestDto;
import com.garnicsoft.dto.LoginByEmailAndPassRequestDto;
import com.garnicsoft.dto.LoginSuccessfulResponseDto;
import com.garnicsoft.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
  public Mono<ResponseEntity<LoginSuccessfulResponseDto>> loginByEmailAndPass(
      @Valid @RequestBody Mono<LoginByEmailAndPassRequestDto> request) {

    return request
        .flatMap(authService::login)
        .map(ResponseEntity::ok)
        .switchIfEmpty(
            Mono.defer(() -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())));
  }

  @PostMapping(
      value = "/basic-register",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<LoginSuccessfulResponseDto>> signUpBasic(
      @Valid @RequestBody Mono<BasicRegisterRequestDto> request) {

    return request.flatMap(authService::sigUpBasic).map(ResponseEntity::ok);
  }
}
