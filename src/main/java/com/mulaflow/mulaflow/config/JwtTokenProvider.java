package com.mulaflow.mulaflow.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refreshExpiration}")
    private long refreshExpirationInMs;

    private final UserDetailsService userDetailsService;

    public String generateToken(String userId) {
        return buildToken(userId, jwtExpirationInMs);
    }

    public String generateRefreshToken(String userId) {
        return buildToken(userId, refreshExpirationInMs);
    }

    private String buildToken(String userId, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            log.error("Invalid Jwt token", ex);
        }
        return false;
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            String userId = claims.getSubject();
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("Token subject (userId) is empty");
            }

            return userId;
        } catch (Exception ex) {
            log.error("Failed to extract user ID from token {}", ex.getMessage());
            throw new JwtException("Invalid token: " + ex.getMessage());
        }
    }

    public Authentication getAuthentication(String token) {
        String userId = getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        return new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    }
}
