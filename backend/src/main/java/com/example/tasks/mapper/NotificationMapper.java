package com.example.tasks.mapper;

import com.example.tasks.domain.Notification;
import com.example.tasks.dto.NotificationDTO;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toDto(Notification notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .taskId(notification.getTaskId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .creationDate(notification.getCreationDate())
                .build();
    }
}