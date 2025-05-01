package com.mulaflow.mulaflow.service.notification.channels;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.mulaflow.mulaflow.dto.notification.channels.EmailRequest;
import com.mulaflow.mulaflow.exception.NotificationException;
import com.mulaflow.mulaflow.service.auth.UserService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserService userService;

    public void send(EmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String toEmail = userService.getUserById(request.getReceiptId()).getEmail();

            helper.setTo(toEmail);
            helper.setSubject(request.getSubject());
            helper.setText(request.getHtmlContent(), true);

            message.addHeader("X-Mailer", "Mulaflow");

            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Email sending failed.", ex);
            throw new NotificationException("Email Sending failed: " + ex.getMessage());
        }
    }
}
