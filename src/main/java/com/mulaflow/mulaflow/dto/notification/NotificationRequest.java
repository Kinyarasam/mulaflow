package com.mulaflow.mulaflow.dto.notification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.mulaflow.mulaflow.model.notification.NotificationChannel;
import com.mulaflow.mulaflow.model.notification.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String recipientId;
    private NotificationType type;
    private Map<String, Object> variables;

    @Builder.Default
    private Set<NotificationChannel> channels = new HashSet<>();

    @Builder.Default
    private boolean persistInDatabase = true;

    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    public static NotificationRequest create(
        String recipientId,
        NotificationType type,
        Map<String, Object> variables
    ) {
        Objects.requireNonNull(recipientId, "Recepient ID cannot be null");
        Objects.requireNonNull(type, "Notification type cannot be null");
        Objects.requireNonNull(variables, "Variables map cannot be null");

        return NotificationRequest.builder()
            .recipientId(recipientId)
            .type(type)
            .variables(variables)
            .channels(new HashSet<>(type.getDefaultChannels()))
            .build();
    }

    public NotificationRequest addVariable(String key, Object value) {
        this.variables.put(key, value);
        return this;
    }

    public NotificationRequest addChannel(NotificationChannel channel) {
        this.channels.add(channel);
        return this;
    }

    public void validate() {
        if (recipientId == null || recipientId.isBlank()) {
            throw new IllegalArgumentException("Recipient ID is required");
        }

        if (channels.isEmpty()) {
            throw new IllegalArgumentException("Atleast one channel must be specified");
        }

        channels.forEach(channel -> {
            if (!type.supportsChannel(channel)) {
                throw new IllegalArgumentException(
                    "Notification type " + type + " doesn't support channel " + channel
                );
            }
        });
    }
}
