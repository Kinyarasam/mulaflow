package com.mulaflow.mulaflow.service.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mulaflow.mulaflow.config.JwtTokenProvider;
import com.mulaflow.mulaflow.dto.auth.LoginRequestDTO;
import com.mulaflow.mulaflow.dto.auth.LoginResponseDTO;
import com.mulaflow.mulaflow.dto.auth.RegisterRequestDTO;
import com.mulaflow.mulaflow.dto.auth.RegisterResponseDTO;
import com.mulaflow.mulaflow.dto.auth.ResetPasswordCompleteRequestDTO;
import com.mulaflow.mulaflow.dto.auth.ResetPasswordCompleteResponseDTO;
import com.mulaflow.mulaflow.dto.auth.ResetPasswordRequestRequestDTO;
import com.mulaflow.mulaflow.dto.auth.ResetPasswordRequestResponseDTO;
import com.mulaflow.mulaflow.dto.notification.NotificationRequest;
import com.mulaflow.mulaflow.dto.notification.NotificationResponse;
import com.mulaflow.mulaflow.exception.AuthenticationException;
import com.mulaflow.mulaflow.exception.BusinessRuleException;
import com.mulaflow.mulaflow.model.auth.PasswordResetToken;
import com.mulaflow.mulaflow.model.notification.NotificationType;
import com.mulaflow.mulaflow.model.user.User;
import com.mulaflow.mulaflow.repository.auth.PasswordResetTokenRepository;
import com.mulaflow.mulaflow.service.notification.NotificationService;
import com.mulaflow.mulaflow.util.CryptoTokenUtil;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final int PASSWORD_RESET_REQUEST_EXPIRE_HOURS = 2;

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
        if (userService.existsByEmail(normalizedEmail)) {
            log.warn("Registration attempt with existing email: {}", normalizedEmail);
            throw new BusinessRuleException("Email already registered");
        }

        // Check if phoneNumber already exists
        // todo: will add a way to verify the phoneNumbers to ensure consistency.
        if (userService.existsByPhoneNumber(dto.getPhoneNumber())) {
            log.warn("Phone Number already exists");
            throw new BusinessRuleException("Phone number already registered");
        }

        // Save new user Record
        User savedUser = userService.save(
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
        User user;
        try {
            if (StringUtils.hasText(dto.getEmail())) {
                user = userService.findByEmail(dto.getEmail());
                if (user != null) {
                    return user;
                }

                log.warn("Login attempt for non-existent email: {}", dto.getEmail());
                throw new AuthenticationException("Invalid credentials");
            }

            if (StringUtils.hasText(dto.getPhoneNumber())) {
                user = userService.findByPhoneNumber(dto.getPhoneNumber());
                if (user != null) {
                    return user;
                }

                log.warn("Login attempt for non-existent phone: {}", dto.getPhoneNumber());
                throw new AuthenticationException("Invalid credentials");
            }

            if (StringUtils.hasText(dto.getUsername())) {
                user = userService.findByUsername(dto.getUsername());
                if (user != null) {
                    return user;
                }
    
                log.warn("Login attempt for non-existent username: {}", dto.getUsername());
                throw new AuthenticationException("Invalid credentials");
            }
        } catch (Exception ex) {
            log.error("Error during user lookup", ex);
            throw new AuthenticationException("Authentication failed");
        }

        throw new AuthenticationException("Invalid credentials");
    }

    @Transactional
    public ResetPasswordRequestResponseDTO initiatePasswordReset(ResetPasswordRequestRequestDTO dto) {
        String normalizedEmail = dto.getEmail().toLowerCase(Locale.ROOT).trim();
        User user = userService.findByEmail(normalizedEmail);

        if (user == null) {
            // For security reasons, don't reveal if email doesn't exist
            log.info("Password reset requested for non-existent email: {}", normalizedEmail);
            return ResetPasswordRequestResponseDTO.builder()
                    .message("If an account with this email exists, a reset link has been sent")
                    .build();
        }

        // Delete any existing tokens for user
        resetTokenRepository.deleteAllByUser(user);
        resetTokenRepository.flush();

        String token = CryptoTokenUtil.generateToken();
        String tokenHash = CryptoTokenUtil.hashToken(token);
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(PASSWORD_RESET_REQUEST_EXPIRE_HOURS);

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .expiryDate(expiryDate)
            .token(tokenHash)
            .user(user)
            .build();
        resetTokenRepository.save(resetToken);

        String resetLink = String.format("%s?token=%s", dto.getRedirectUrl(), token);

        // Debug: Verify variables map
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getEmail());
        variables.put("resetLink", resetLink);
        variables.put("expiryHours", PASSWORD_RESET_REQUEST_EXPIRE_HOURS);
 
        NotificationRequest notificationRequest = NotificationRequest.create(
            user.getId(),
            NotificationType.PASSWORD_RESET_REQUEST,
            variables
        );

        notificationService.send(notificationRequest);

        log.info("Password reset token generated for user {}: {}", user.getEmail(), resetToken);
        return ResetPasswordRequestResponseDTO.builder()
            .message("If an account with this email exists, a reset link has been sent")
            .build();
    }

    @Transactional
    public ResetPasswordCompleteResponseDTO completePasswordReset(String token, ResetPasswordCompleteRequestDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessRuleException("Passwords do not match");
        }

        List<PasswordResetToken> tokens = resetTokenRepository.findAllByExpiryDateAfter(LocalDateTime.now());
        for (PasswordResetToken resetToken: tokens) {
            if (CryptoTokenUtil.verifyToken(token, resetToken.getToken())) {
                if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                    throw new BusinessRuleException("Password reset token has expired");
                }
        
                User user = resetToken.getUser();
                user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                userService.save(user);
        
                // Delete the used token
                resetTokenRepository.delete(resetToken);
        
                return ResetPasswordCompleteResponseDTO.builder()
                    .message("Success")
                    .build();
            }
        }
        throw new BusinessRuleException("Invalid or expired password reset token");
    }
}
