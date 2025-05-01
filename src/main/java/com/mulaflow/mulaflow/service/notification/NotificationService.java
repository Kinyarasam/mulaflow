package com.mulaflow.mulaflow.service.notification;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mulaflow.mulaflow.dto.notification.NotificationDTO;
import com.mulaflow.mulaflow.dto.notification.NotificationRequest;
import com.mulaflow.mulaflow.dto.notification.NotificationResponse;
import com.mulaflow.mulaflow.dto.notification.channels.EmailRequest;
import com.mulaflow.mulaflow.exception.ResourceNotFoundException;
import com.mulaflow.mulaflow.model.notification.Notification;
import com.mulaflow.mulaflow.model.notification.NotificationDelivery;
import com.mulaflow.mulaflow.model.notification.NotificationChannelStatus.DeliveryStatus;
import com.mulaflow.mulaflow.repository.NotificationDeliveryRepository;
import com.mulaflow.mulaflow.repository.NotificationRepository;
import com.mulaflow.mulaflow.service.auth.UserService;
import com.mulaflow.mulaflow.service.notification.channels.EmailService;
import com.mulaflow.mulaflow.service.notification.templates.NotificationTemplateEngine;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationTemplateEngine templateEngine;
    private final UserService userService;
    private final EmailService emailService;
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(5);

    @Transactional
    public NotificationResponse send(NotificationRequest request) {

        request.validate();
        String content = templateEngine.processTemplate(
            request.getType(),
            request.getVariables()
        );
        Notification notification = Notification.builder()
            .recipient(userService.getUserById(request.getRecipientId()))
            .type(request.getType())
            .content(content)
            .deliveryChannels(request.getChannels())
            .recipient(userService.getUserById(request.getRecipientId()))
            .build();
        
        Notification savedNotification = notificationRepository.save(notification);

        // Save the notification deliveries
        List<NotificationDelivery> deliveries = notification.getDeliveryChannels().stream()
            .map(channel -> NotificationDelivery.builder()
                .notification(notification)
                .channel(channel)
                .status(DeliveryStatus.PENDING)
                .build())
            .collect(Collectors.toList());
        
        deliveryRepository.saveAll(deliveries);
        notification.setDeliveries(deliveries);

        asyncExecutor.submit(() -> processDeliveries(savedNotification));

        return NotificationResponse.builder()
                .notificationId(savedNotification.getId())
                .type(savedNotification.getType().toString())
                .status("PROCESSING")
                .build();
    }

    @Transactional
    public void processPendingDeliveries() {
        List<NotificationDelivery> pendingDeliveries = deliveryRepository.findByStatus(DeliveryStatus.PENDING);
        
        pendingDeliveries.forEach(this::processDelivery);
    }

    @Async
    private void processDeliveries(Notification notification) {
        notification.getDeliveries().forEach(delivery -> {
            try {
                processDelivery(delivery);
            } catch (Exception ex) {
                log.error("failed to process delivery {} for notification {}",
                    delivery.getId(), notification.getId(), ex);
            }
        });
    }

    private void processDelivery(NotificationDelivery delivery) {
        try {
            String externalId = switch (delivery.getChannel()) {
                case EMAIL -> sendEmail(delivery);
                case SMS -> sendSMS(delivery);
                case WHATSAPP -> sendWhatsApp(delivery);
                case PUSH -> sendPush(delivery);
                case IN_APP -> sendInApp(delivery);
            };

            delivery.setExternalId(externalId);
            delivery.setSentAt(Instant.now());
            delivery.setStatus(DeliveryStatus.SENT);
        } catch (Exception e) {
            delivery.setErrorMessage(e.getMessage());
            delivery.setStatus(DeliveryStatus.FAILED);
            delivery.setFailedAt(Instant.now());
        } finally {
            log.info("deliver {} updated.", delivery.getId());
            deliveryRepository.saveAndFlush(delivery);
        }
    }

    private String sendEmail(NotificationDelivery request) {
        emailService.send(
            EmailRequest.builder()
                .subject(request.getNotification().getType().getDefaultTitle())
                .receiptId(request.getNotification().getRecipient().getId())
                .htmlContent(request.getNotification().getContent())
                .build()
        );
        return "";
    }
    private String sendSMS(NotificationDelivery request) {return "";}
    private String sendWhatsApp(NotificationDelivery request) {return "";}
    private String sendInApp(NotificationDelivery request) {return "";}
    private String sendPush(NotificationDelivery request) {return "";}


    @Transactional
    public Notification getNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    public NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder().build();
    }
}
