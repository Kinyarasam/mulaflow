package com.mulaflow.mulaflow.model.notification;

import java.util.Set;

import com.mulaflow.mulaflow.model.BaseModel;
import com.mulaflow.mulaflow.model.user.User;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseModel {
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne
    private User recipient;

    private String content;
    private boolean isRead;

    @ElementCollection
    @Enumerated
    private Set<NotificationChannel> deliverChannels;
}
