package com.example.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCommentDTO {

    private Long commentId;
    private Long taskId;
    private Long userId;
    private String username;

    @NotBlank
    @Size(max = 2000)
    private String body;

    private LocalDateTime creationDate;
}