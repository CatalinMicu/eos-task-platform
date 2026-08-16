package com.example.tasks.controller;

import com.example.tasks.dto.TaskCommentDTO;
import com.example.tasks.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @GetMapping
    public List<TaskCommentDTO> getComments(
            @PathVariable Long taskId
    ) {
        return taskCommentService.getComments(taskId);
    }

    @PostMapping
    public TaskCommentDTO addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentDTO commentDTO
    ) {
        return taskCommentService.addComment(taskId, commentDTO);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId
    ) {
        taskCommentService.deleteComment(taskId, commentId);
    }
}
