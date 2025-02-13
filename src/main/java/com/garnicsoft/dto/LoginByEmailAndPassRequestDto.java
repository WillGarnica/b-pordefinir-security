package com.garnicsoft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginByEmailAndPassRequestDto implements Serializable {

  @NotBlank @Email private String email;

  @NotBlank private String pass;
}
