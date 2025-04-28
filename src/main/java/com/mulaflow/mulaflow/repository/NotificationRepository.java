package com.mulaflow.mulaflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mulaflow.mulaflow.model.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    
}
