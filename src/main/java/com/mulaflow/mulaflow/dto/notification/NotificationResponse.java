package com.mulaflow.mulaflow.dto.notification;

import java.time.Instant;
import java.util.Map;

import com.mulaflow.mulaflow.model.notification.NotificationChannel;
import com.mulaflow.mulaflow.model.notification.NotificationChannelStatus.DeliveryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String notificationId;
    private String type;
    private String status;
    private Map<NotificationChannel, DeliveryStatus> channelStatuses;
    private Instant sentAt;
}
