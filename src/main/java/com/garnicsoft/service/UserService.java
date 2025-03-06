package com.garnicsoft.service;

import com.garnicsoft.entity.User;
import com.garnicsoft.repository.UserRepository;
import java.time.LocalDateTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final UserRepository userRepository;

  private final short securityLoginMaxFailedAttempts;
  private final short maxMinutesBetweenFailedAttempts;

  public UserService(
      UserRepository userRepository,
      @Value("${security.login.max.failed.attempts}") short securityLoginMaxFailedAttempts,
      @Value("${security.login.max.minutes.between.failed.attempts}")
          short maxMinutesBetweenFailedAttempts) {
    this.securityLoginMaxFailedAttempts = securityLoginMaxFailedAttempts;
    this.userRepository = userRepository;
    this.maxMinutesBetweenFailedAttempts = maxMinutesBetweenFailedAttempts;
  }

  public Mono<User> getUserByEmail(String email) {
    if (StringUtils.isBlank(email)) Mono.error(new IllegalArgumentException());

    return userRepository.findByEmailIgnoreCaseAndPasswordIsNotNull(email.toLowerCase());
  }

  public Mono<User> save(User user) {

    user.setUpdatedAt(user.getUpdatedAt() == null ? null : LocalDateTime.now());
    return userRepository.save(user);
  }

  public Mono<User> add1FailedLoginAttempt(User user) {
    if (user == null || user.getId() == null) return Mono.empty();

    short newFailedLoginAttemptsAmount = (short) (user.getFailedLoginAttemptsAmount() + 1);

    if (user.getLastFailedLoginAttemptDate() == null
        || user.getLastFailedLoginAttemptDate()
            .isBefore(LocalDateTime.now().minusMinutes(maxMinutesBetweenFailedAttempts))) {
      newFailedLoginAttemptsAmount = 1;
    }

    if (newFailedLoginAttemptsAmount >= this.securityLoginMaxFailedAttempts) {
      user.setLockedAt(LocalDateTime.now());
      user.setFailedLoginAttemptsAmount((short) 0);
      user.setLastFailedLoginAttemptDate(null);
    } else {
      user.setFailedLoginAttemptsAmount(newFailedLoginAttemptsAmount);
      user.setLockedAt(null);
      user.setLastFailedLoginAttemptDate(LocalDateTime.now());
    }

    return save(user);
  }

  public Mono<User> resetAmountFailedLoginAttempts(User user) {
    if (user == null || user.getId() == null) return Mono.empty();

    user.setFailedLoginAttemptsAmount((short) 0);
    user.setLastFailedLoginAttemptDate(null);
    user.setLockedAt(null);
    return save(user);
  }
}
