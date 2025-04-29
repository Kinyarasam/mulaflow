package com.mulaflow.mulaflow.model.notification;

import java.time.Instant;

import com.mulaflow.mulaflow.model.BaseModel;
import com.mulaflow.mulaflow.model.notification.NotificationChannelStatus.DeliveryStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notification_deliveries")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDelivery extends BaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private String externalId;
    private Instant sentAt;
    private Instant failedAt;
    private Instant deliveredAt;
    private Instant readAt;
    private String errorMessage;
}
