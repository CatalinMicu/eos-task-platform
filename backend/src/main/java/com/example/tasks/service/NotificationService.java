package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.Notification;
import com.example.tasks.dto.NotificationDTO;
import com.example.tasks.mapper.NotificationMapper;
import com.example.tasks.repository.NotificationsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationsRepository notificationsRepository;
    private final NotificationMapper notificationMapper;
    private final PermissionChecker permissionChecker;

    public List<NotificationDTO> getCurrentNotifications() {
        Long userId = permissionChecker
                .getCurrentUser()
                .getUserId();

        List<Notification> notifications =
                notificationsRepository
                        .findAllByUserIdOrderByCreationDateDesc(userId);

        List<NotificationDTO> notificationDTOs =
                new ArrayList<>();

        for (Notification notification : notifications) {
            notificationDTOs.add(
                    notificationMapper.toDto(notification)
            );
        }

        return notificationDTOs;
    }

    @Transactional
    public void createNotification(
            Long userId,
            Long taskId,
            String message
    ) {
        Notification notification = Notification.builder()
                .userId(userId)
                .taskId(taskId)
                .message(message)
                .isRead(0)
                .build();

        notificationsRepository.save(notification);
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        Long userId = permissionChecker
                .getCurrentUser()
                .getUserId();

        Notification notification = notificationsRepository
                .findByNotificationIdAndUserId(
                        notificationId,
                        userId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found"
                        )
                );

        notification.setIsRead(1);

        Notification savedNotification =
                notificationsRepository.save(notification);

        return notificationMapper.toDto(savedNotification);
    }
}