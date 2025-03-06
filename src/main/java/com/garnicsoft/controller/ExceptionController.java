package com.garnicsoft.controller;

import com.garnicsoft.dto.ErrorDto;
import io.jsonwebtoken.lang.Collections;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

  @ExceptionHandler(value = {WebExchangeBindException.class})
  public Mono<ResponseEntity<ErrorDto>> handle(@NotNull WebExchangeBindException e) {
    ErrorDto errorDto = ErrorDto.builder().message(e.getReason()).build();

    if (!Collections.isEmpty(e.getAllErrors()))
      errorDto.setErrors(
          e.getAllErrors().stream()
              .map(
                  error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + " " + errorMessage;
                  })
              .toList());

    return Mono.just(ResponseEntity.badRequest().body(errorDto));
  }

  @ExceptionHandler(value = {ConstraintViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Mono<ErrorDto> handle(@NotNull ConstraintViolationException e) {
    ErrorDto errorDto = ErrorDto.builder().message(e.getCause().getMessage()).build();

    if (!Collections.isEmpty(e.getConstraintViolations()))
      errorDto.setErrors(
          e.getConstraintViolations().stream()
              .map(
                  violation -> {
                    String[] pathParts = violation.getPropertyPath().toString().split("\\.");
                    return pathParts[pathParts.length - 1] + " " + violation.getMessage();
                  })
              .toList());

    return Mono.just(errorDto);
  }

  @ExceptionHandler(
      value = {
        LockedException.class,
      })
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Mono<ErrorDto> handleForbidden(@NotNull Exception e) {
    return Mono.just(getErrorDtoFromException(e));
  }

  @ExceptionHandler(
      value = {
        IllegalArgumentException.class,
        ServerWebInputException.class,
      })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Mono<ErrorDto> handle(@NotNull Exception e) {
    return Mono.just(getErrorDtoFromException(e));
  }

  @ExceptionHandler(value = {UnsupportedMediaTypeStatusException.class})
  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  public Mono<ErrorDto> handle(@NotNull UnsupportedMediaTypeStatusException e) {
    ErrorDto error = getErrorDtoFromException(e);
    error.setMessage(
        e.getReason()
            + ", SupportedMediaTypes: "
            + Arrays.toString(e.getSupportedMediaTypes().toArray()));
    return Mono.just(error);
  }

  @ExceptionHandler(value = {Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Mono<ErrorDto> handleGeneric(@NotNull Exception e) {
    return Mono.just(getErrorDtoFromException(e));
  }

  private ErrorDto getErrorDtoFromException(@NotNull Exception e) {
    if (e == null) return new ErrorDto();

    log.error("ERROR: ", e);
    return ErrorDto.builder().message(e.getLocalizedMessage()).build();
  }
}
