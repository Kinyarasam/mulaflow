package com.mulaflow.mulaflow.service.notification.templates;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationTemplate {
    private String subjectTemplate;
    private String bodyTemplate;
    private String smsTemplate;
}
