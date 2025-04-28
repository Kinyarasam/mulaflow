package com.mulaflow.mulaflow.service.notification;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mulaflow.mulaflow.dto.notification.NotificationRequest;
import com.mulaflow.mulaflow.dto.notification.NotificationResponse;
import com.mulaflow.mulaflow.model.notification.Notification;
import com.mulaflow.mulaflow.repository.NotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    
    @Async
    public NotificationResponse send(NotificationRequest request) {

        request.validate();
        if (request.isPersistInDatabase()) {
            Notification notification = createNotification(request);
        }

        NotificationResponse response = new NotificationResponse();
    
        request.getChannels().forEach(channel -> {
            try {
                switch (channel) {
                    case EMAIL -> sendEmail(request);
                    case SMS -> sendSMS(request);
                    case IN_APP -> SendInApp(request);
                    case PUSH -> sendPush(request);
                    case WHATSAPP -> sendWhatsApp(request);
                }
            } catch (Exception ex) {
                log.error("Failed to send notification", ex);
            }
        });
        return response;
    }

    private void sendEmail(NotificationRequest request) {}
    private void sendSMS(NotificationRequest request) {}
    private void sendWhatsApp(NotificationRequest request) {}
    private void SendInApp(NotificationRequest request) {}
    private void sendPush(NotificationRequest request) {}

    @Transactional
    private Notification createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .content(null)
                .deliverChannels(null)
                .isRead(false)
                .recipient(null)
                .build();

        notificationRepository.save(notification);
        return notification;
    }
}
