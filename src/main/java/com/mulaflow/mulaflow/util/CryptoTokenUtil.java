package com.mulaflow.mulaflow.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CryptoTokenUtil {

    private static SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 64;
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    public static String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String hashToken(String token) {
        try {
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);

            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(
                token.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
            );

            SecretKey key = skf.generateSecret(spec);
            byte[] hash = key.getEncoded();

            // Combine salt and key for storage.
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            log.error("Error hashing token", ex);
            throw new RuntimeException("Error hashing token", ex);
        }
    }

    public static boolean verifyToken(String inputToken, String storedToken) {
        byte[] combined = Base64.getDecoder().decode(storedToken);
        byte[] salt = new byte[16];
        System.arraycopy(combined, 0, salt, 0, salt.length);

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(
                inputToken.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
            );

            SecretKey key = skf.generateSecret(spec);
            byte[] testHash = key.getEncoded();

            // compare the hashes
            int diff = combined.length - 16 - testHash.length;
            if (diff != 0) return false;

            for (int i = 0; i < testHash.length; i++) {
                if (testHash[i] != combined[16 + i]) {
                    return false;
                }
            }

            return true;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            log.error("Error verifying token", ex);
            throw new RuntimeException("Error hashing token", ex);
        }
    }
}