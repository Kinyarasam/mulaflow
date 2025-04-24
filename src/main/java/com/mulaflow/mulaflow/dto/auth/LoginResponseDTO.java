package com.mulaflow.mulaflow.dto.auth;

import com.mulaflow.mulaflow.dto.user.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String refreshToken;
    private String deviceId;
    private int activeSessions;
    private UserDTO user;
}
