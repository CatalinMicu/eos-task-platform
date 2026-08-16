package com.example.tasks.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO {

    private Long notificationId;
    private Long taskId;
    private String message;
    private Integer isRead;
    private LocalDateTime creationDate;
}
