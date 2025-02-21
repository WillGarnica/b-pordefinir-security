package com.garnicsoft.service;

import com.garnicsoft.dto.BasicRegisterRequestDto;
import com.garnicsoft.dto.LoginByEmailAndPassRequestDto;
import com.garnicsoft.dto.LoginSuccessfulResponseDto;
import com.garnicsoft.entity.User;
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

  public Mono<LoginSuccessfulResponseDto> login(LoginByEmailAndPassRequestDto loginInfo) {

    if (loginInfo == null
        || StringUtils.isBlank(loginInfo.getEmail())
        || StringUtils.isBlank(loginInfo.getPass())) throw new IllegalArgumentException();

    return this.userService
        .getUserByEmail(loginInfo.getEmail())
        .filter(user -> arePassMatches(loginInfo.getPass(), user.getPassword()))
        .map(this::getLoginResponseFromUser);
  }

  private boolean arePassMatches(String pass1, String pass2) {
    if (StringUtils.isBlank(pass1) || StringUtils.isBlank(pass2))
      throw new IllegalArgumentException();

    return passwordEncoder.matches(pass1, pass2);
  }

  private LoginSuccessfulResponseDto getLoginResponseFromUser(User user) {
    if (user == null) return null;

    Map<String, Object> rolesMap = Map.of("roles", Map.of());
    String authToken = jwtService.generateAuthToken(user.getEmail(), rolesMap);
    String refreshToken = jwtService.generateRefreshToken(user.getEmail(), rolesMap);

    return LoginSuccessfulResponseDto.builder()
        .authToken(authToken)
        .refreshToken(refreshToken)
        .active(user.isActive())
        .build();
  }

  public Mono<LoginSuccessfulResponseDto> sigUpBasic(BasicRegisterRequestDto registerInfo) {

    if (registerInfo == null
        || StringUtils.isBlank(registerInfo.getEmail())
        || StringUtils.isBlank(registerInfo.getPass())) throw new IllegalArgumentException();

    User user =
        User.builder()
            .email(registerInfo.getEmail())
            .active(true)
            .password(passwordEncoder.encode(registerInfo.getPass()))
            .build();

    // un registro exitoso es similar a un login, por lo que se retorna un LoginSuccessfulResponse
    return userService.save(user).map(this::getLoginResponseFromUser);
  }
}
