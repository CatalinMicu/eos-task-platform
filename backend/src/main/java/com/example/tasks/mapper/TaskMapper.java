package com.example.tasks.mapper;

import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.TaskDTO;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDTO toDto(Task task) {
        String statusName = null;
        if (task.getStatusType() != null) {
            statusName = task.getStatusType().getStatusName();
        }

        Long userId = null;
        String assignedTo = null;
        if (task.getUser() != null) {
            userId = task.getUser().getUserId();
            assignedTo = task.getUser().getUsername();
        }

        return TaskDTO.builder()
                .taskId(task.getTaskId())
                .name(task.getName())
                .dueDate(task.getDueDate())
                .statusName(statusName)
                .userId(userId)
                .assignedTo(assignedTo)
                .createdBy(task.getCreatedBy())
                .body(task.getBody())
                .build();
    }

    public Task toEntity(TaskDTO taskDTO, User user, StatusType statusType) {
        return Task.builder()
                .name(taskDTO.getName())
                .dueDate(taskDTO.getDueDate())
                .user(user)
                .statusType(statusType)
                .body(taskDTO.getBody())
                .build();
    }
}
