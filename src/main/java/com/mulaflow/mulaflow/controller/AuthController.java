package com.mulaflow.mulaflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mulaflow.mulaflow.dto.auth.LoginRequestDTO;
import com.mulaflow.mulaflow.dto.auth.LoginResponseDTO;
import com.mulaflow.mulaflow.dto.auth.RegisterRequestDTO;
import com.mulaflow.mulaflow.dto.auth.RegisterResponseDTO;
import com.mulaflow.mulaflow.exception.AuthenticationException;
import com.mulaflow.mulaflow.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
        @Valid @RequestBody RegisterRequestDTO dto
    ) {
        RegisterResponseDTO response = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
        @Valid @RequestBody LoginRequestDTO dto,
        @RequestHeader("User-Agent") String userAgent,
        HttpServletRequest request
    ) throws AuthenticationException {
        log.info("Login attempt for email: {}", dto.getEmail());
        LoginResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
}
