package com.mulaflow.mulaflow.model.notification;

import java.util.Arrays;
import java.util.List;

public enum NotificationType {
    PASSWORD_RESET_REQUEST(
        "Password Reset Request",
        "password_reset_request",
        List.of(NotificationChannel.EMAIL)
    );

    private final String defaultTitle;
    private final String templateName;
    private final List<NotificationChannel> defaultChannels;

    NotificationType(
        String defaultTitle,
        String templateName,
        List<NotificationChannel> defaultChannels
    ) {
        this.defaultTitle = defaultTitle;
        this.templateName = templateName;
        this.defaultChannels = defaultChannels;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public List<NotificationChannel> getDefaultChannels () {
        return defaultChannels;
    }

    public String getTemplateName() {
        return templateName;
    }

    public static NotificationType fromString(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid notification type"));
    }

    public boolean supportsChannel(NotificationChannel channel) {
        return defaultChannels.contains(channel);
    }
}
