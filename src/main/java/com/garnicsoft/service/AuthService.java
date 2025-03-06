package com.garnicsoft.service;

import com.garnicsoft.dto.BasicRegisterRequestDto;
import com.garnicsoft.dto.LoginByEmailAndPassRequestDto;
import com.garnicsoft.dto.LoginSuccessfulResponseDto;
import com.garnicsoft.entity.User;
import java.time.LocalDateTime;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final int securityLoginLockTimeMinutes;

  public AuthService(
      UserService userService,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      @Value("${security.login.lock.time.minutes}") int securityLoginLockTimeMinutes) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.securityLoginLockTimeMinutes = securityLoginLockTimeMinutes;
  }

  public Mono<LoginSuccessfulResponseDto> login(LoginByEmailAndPassRequestDto loginInfo) {

    if (loginInfo == null
        || StringUtils.isBlank(loginInfo.getEmail())
        || StringUtils.isBlank(loginInfo.getPass())) throw new IllegalArgumentException();

    return this.userService
        .getUserByEmail(loginInfo.getEmail())
        .filter(user -> !isFailedLoginAttemptsUserBlocked(user))
        .filter(user -> arePassMatches(loginInfo.getPass(), user))
        .map(this::getLoginResponseFromUser);
  }

  private boolean isFailedLoginAttemptsUserBlocked(User user) {
    if (user != null
        && user.getLockedAt() != null
        && user.getLockedAt()
            .isAfter(LocalDateTime.now().minusMinutes(this.securityLoginLockTimeMinutes))) {
      throw new LockedException("Too many login attempts for this user");
    }

    return false;
  }

  private boolean arePassMatches(String pass1, User user) {
    if (user == null || StringUtils.isBlank(pass1) || StringUtils.isBlank(user.getPassword()))
      throw new IllegalArgumentException();

    boolean isCorrectPass = passwordEncoder.matches(pass1, user.getPassword());

    if (isCorrectPass) userService.resetAmountFailedLoginAttempts(user).subscribe();
    else userService.add1FailedLoginAttempt(user).subscribe();

    return isCorrectPass;
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
