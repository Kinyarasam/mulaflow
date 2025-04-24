package com.mulaflow.mulaflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mulaflow.mulaflow.model.user.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
