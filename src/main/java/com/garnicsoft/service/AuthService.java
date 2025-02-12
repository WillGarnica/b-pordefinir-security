package com.garnicsoft.service;

import com.garnicsoft.dto.BasicRegisterRequest;
import com.garnicsoft.dto.LoginByEmailAndPassRequest;
import com.garnicsoft.dto.LoginSuccessfulResponse;
import com.garnicsoft.entity.User;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public Mono<LoginSuccessfulResponse> login(
      @Valid LoginByEmailAndPassRequest loginByEmailAndPassRequest) {
    return this.userService
        .getUserByEmail(loginByEmailAndPassRequest.getEmail())
        .filter(user -> arePassMatches(loginByEmailAndPassRequest.getPass(), user.getPassword()))
        .map(this::getLoginResponseFromUser);
  }

  private boolean arePassMatches(String pass1, String pass2) {
    if (StringUtils.isBlank(pass1) || StringUtils.isBlank(pass2))
      throw new IllegalArgumentException();

    return passwordEncoder.matches(pass1, pass2);
  }

  private LoginSuccessfulResponse getLoginResponseFromUser(User user) {
    if (user == null) return null;

    String authToken = jwtService.generateToken(user.getEmail(), Map.of("roles", Map.of()));
    return LoginSuccessfulResponse.builder().authToken(authToken).active(user.isActive()).build();
  }

  public Mono<LoginSuccessfulResponse> sigUpBasic(@Valid BasicRegisterRequest request) {
    User user =
        User.builder()
            .email(request.getEmail())
            .active(true)
            .password(passwordEncoder.encode(request.getPass()))
            .build();

    // un registro exitoso es similar a un login, por lo que se retorna un LoginSuccessfulResponse
    return userService.save(user).map(this::getLoginResponseFromUser);
  }
}
