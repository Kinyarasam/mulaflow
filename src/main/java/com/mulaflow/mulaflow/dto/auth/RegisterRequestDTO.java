package com.mulaflow.mulaflow.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First Name must be between 2-100 characters")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    @Size(min = 2, max = 50, message = "Last Name must be between 2-100 characters")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 50, message = "username must be between 2-100 characters")
    private String username;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    @Pattern(regexp = ".+@.+\\..+", message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must contain at least 1 uppercase, 1 lowercase, 1 number and 1 special character"
    )
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    @AssertTrue(message = "Passwords must match")
    private boolean isPasswordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+(?:[0-9] ?){6,14}[0-9]$", 
        message = "Phone number must be in international format (+XXX XXX XXXX)"
    )
    private String phoneNumber;

    @AssertTrue(message = "You must agree to the terms and conditions")
    private boolean termsAccepted;
}