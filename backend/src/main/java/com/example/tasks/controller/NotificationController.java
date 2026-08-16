package com.example.tasks.controller;

import com.example.tasks.dto.NotificationDTO;
import com.example.tasks.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDTO> getCurrentNotifications() {
        return notificationService.getCurrentNotifications();
    }

    @PatchMapping("/{id}/read")
    public NotificationDTO markAsRead(
            @PathVariable Long id
    ) {
        return notificationService.markAsRead(id);
    }
}