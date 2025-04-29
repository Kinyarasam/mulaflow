package com.mulaflow.mulaflow.model.notification;

import com.mulaflow.mulaflow.model.BaseModel;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notification_delivery_status")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationChannelStatus extends BaseModel {

    @Enumerated(EnumType.STRING)
    private NotificationType channel;

    @OneToOne
    private Notification notification;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    public enum DeliveryStatus {
        PENDING,
        PROCESSING,
        FAILED,
        SENT,
        DELIVERED,
        READ
    }
}
