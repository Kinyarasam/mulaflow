package com.mulaflow.mulaflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mulaflow.mulaflow.model.notification.NotificationChannelStatus.DeliveryStatus;
import com.mulaflow.mulaflow.model.notification.NotificationDelivery;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, String> {

    List<NotificationDelivery> findByStatus(DeliveryStatus pending);
    
}
