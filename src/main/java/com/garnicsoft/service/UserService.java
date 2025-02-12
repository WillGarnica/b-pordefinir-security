package com.garnicsoft.service;

import com.garnicsoft.entity.User;
import com.garnicsoft.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Mono<User> getUserByEmail(String email) {
    if (StringUtils.isBlank(email)) Mono.error(new IllegalArgumentException());

    return userRepository.findByEmailIgnoreCaseAndPasswordIsNotNull(email.toLowerCase());
  }

  public Mono<User> save(User user) {
    return userRepository.save(user);
  }
}
