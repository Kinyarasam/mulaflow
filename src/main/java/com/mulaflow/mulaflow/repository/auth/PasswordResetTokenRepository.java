package com.mulaflow.mulaflow.repository.auth;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mulaflow.mulaflow.model.auth.PasswordResetToken;
import com.mulaflow.mulaflow.model.user.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    PasswordResetToken findByToken(String token);
    List<PasswordResetToken> findAllByUser(User user);
    List<PasswordResetToken> findAllByExpiryDateAfter(LocalDateTime expiryDate);
    void deleteAllByUser(User user);
}
