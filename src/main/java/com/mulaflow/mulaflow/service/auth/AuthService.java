package com.mulaflow.mulaflow.service.auth;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mulaflow.mulaflow.config.JwtTokenProvider;
import com.mulaflow.mulaflow.dto.auth.*;
import com.mulaflow.mulaflow.exception.*;
import com.mulaflow.mulaflow.model.user.User;
import com.mulaflow.mulaflow.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) throws AuthenticationException {
        // Validate request
        if (
            dto == null
            || (
                !StringUtils.hasText(dto.getEmail()) 
                && !StringUtils.hasText(dto.getPhoneNumber())
                && !StringUtils.hasText(dto.getUsername())
            )
        ) {
            log.warn("Login attempt with empty credentials");
            throw new AuthenticationException("Email, phone number, or username is required");
        }

        if (!StringUtils.hasText(dto.getPassword())) {
            log.warn("Login attempt with empty password");
            throw new AuthenticationException("Password is required");
        }
    
        User user = findUserByCredentials(dto);

        log.info("User: {}", user);

        if (user == null) {
            throw new AuthenticationException("Invalid credentials");
        }

        log.info("input: {} -> compare: {}", dto.getPassword(), user.getPassword());
        // Verify password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", user.getEmail());
            throw new AuthenticationException("Invalid credentials");
        }

        String token = tokenProvider.generateToken(user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        return LoginResponseDTO.builder()
                .user(userService.mapToDTO(user))
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    public RegisterResponseDTO register(RegisterRequestDTO dto) {
        // Check if email already exists
        String normalizedEmail = dto.getEmail().toLowerCase(Locale.ROOT).trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Registration attempt with existing email: {}", normalizedEmail);
            throw new BusinessRuleException("Email already registered");
        }

        // Check if phoneNumber already exists
        // todo: will add a way to verify the phoneNumbers to ensure consistency.
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            log.warn("Phone Number already exists");
            throw new BusinessRuleException("Phone number already registered");
        }

        // Save new user Record
        User savedUser = userRepository.save(
                User.builder()
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .username(dto.getUsername())
                    .email(normalizedEmail)
                    .phoneNumber(dto.getPhoneNumber())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .build()
            );

        // Return the new info
        return RegisterResponseDTO.builder()
                .id(savedUser.getId())
                .name(savedUser.getFirstName() + " " + savedUser.getLastName())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }


private User findUserByCredentials(LoginRequestDTO dto) throws AuthenticationException {
    try {
        if (StringUtils.hasText(dto.getEmail())) {
            return userRepository.findByEmail(dto.getEmail().toLowerCase())
                    .orElseThrow(() -> {
                        log.warn("Login attempt for non-existent email: {}", dto.getEmail());
                        return new AuthenticationException("Invalid credentials");
                    });
        }

        if (StringUtils.hasText(dto.getPhoneNumber())) {
            return userRepository.findByPhoneNumber(dto.getPhoneNumber())
                    .orElseThrow(() -> {
                        log.warn("Login attempt for non-existent phone: {}", dto.getPhoneNumber());
                        return new AuthenticationException("Invalid credentials");
                    });
        }

        if (StringUtils.hasText(dto.getUsername())) {
            return userRepository.findByUsername(dto.getUsername())
                    .orElseThrow(() -> {
                        log.warn("Login attempt for non-existent username: {}", dto.getUsername());
                        return new AuthenticationException("Invalid credentials");
                    });
        }
    } catch (Exception ex) {
        log.error("Error during user lookup", ex);
        throw new AuthenticationException("Authentication failed");
    }

    throw new AuthenticationException("Invalid credentials");
}
}
