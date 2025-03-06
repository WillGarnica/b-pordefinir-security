package com.garnicsoft.entity;

import jakarta.persistence.Column;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "security.users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id private Long id;

  private String email;

  @Column private String password;

  @Column private boolean active;

  @Column private LocalDateTime createdAt;

  @Column private LocalDateTime updatedAt;

  @Column private LocalDateTime lockedAt;

  @Column private LocalDateTime lastFailedLoginAttemptDate;

  @Column private Short failedLoginAttemptsAmount;
}
