package com.example.tasks.mapper;

import com.example.tasks.domain.TaskComment;
import com.example.tasks.dto.TaskCommentDTO;
import org.springframework.stereotype.Component;

@Component
public class TaskCommentMapper {

    public TaskCommentDTO toDto(TaskComment comment) {
        return TaskCommentDTO.builder()
                .commentId(comment.getCommentId())
                .taskId(comment.getTask().getTaskId())
                .userId(comment.getUser().getUserId())
                .username(comment.getUser().getUsername())
                .body(comment.getBody())
                .creationDate(comment.getCreationDate())
                .build();
    }
}
