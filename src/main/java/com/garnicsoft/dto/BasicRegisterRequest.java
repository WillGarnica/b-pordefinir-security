package com.garnicsoft.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicRegisterRequest implements Serializable {

  @NotBlank private String email;

  @NotBlank private String pass;
}
