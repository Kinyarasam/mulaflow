package com.mulaflow.mulaflow.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestRequestDTO {
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "redirect Url is required")
    private String redirectUrl;
}
