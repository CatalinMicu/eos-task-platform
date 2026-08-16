package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.TaskComment;
import com.example.tasks.domain.User;
import com.example.tasks.dto.TaskCommentDTO;
import com.example.tasks.mapper.TaskCommentMapper;
import com.example.tasks.repository.TaskCommentRepository;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskCommentMapper taskCommentMapper;
    private final PermissionChecker permissionChecker;
    private final NotificationService notificationService;

    public List<TaskCommentDTO> getComments(Long taskId) {
        Task task = findTask(taskId);
        requireTaskAccess(task);

        List<TaskCommentDTO> commentDTOs = new ArrayList<>();
        List<TaskComment> comments =
                taskCommentRepository.findAllByTask_TaskIdOrderByCreationDateAsc(taskId);

        for (TaskComment comment : comments) {
            commentDTOs.add(taskCommentMapper.toDto(comment));
        }

        return commentDTOs;
    }

    @Transactional
    public TaskCommentDTO addComment(Long taskId, TaskCommentDTO commentDTO) {
        Task task = findTask(taskId);
        requireTaskAccess(task);

        User currentUser = permissionChecker.getCurrentUser();

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(currentUser)
                .body(commentDTO.getBody())
                .build();

        TaskComment savedComment = taskCommentRepository.save(comment);
        notifyOtherParticipant(task, currentUser);

        return taskCommentMapper.toDto(savedComment);
    }

    @Transactional
    public void deleteComment(Long taskId, Long commentId) {
        Task task = findTask(taskId);
        requireTaskAccess(task);

        TaskComment comment = findComment(taskId, commentId);
        requireCommentOwner(comment);

        taskCommentRepository.delete(comment);
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Task not found: " + taskId)
                );
    }

    private TaskComment findComment(Long taskId, Long commentId) {
        return taskCommentRepository
                .findByCommentIdAndTask_TaskId(commentId, taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Comment not found: " + commentId
                        )
                );
    }

    private void requireTaskAccess(Task task) {
        if (!permissionChecker.canAccessTask(task, "READ")) {
            throw new AccessDeniedException("Access denied for this task");
        }
    }

    private void requireCommentOwner(TaskComment comment) {
        Long currentUserId = permissionChecker
                .getCurrentUser()
                .getUserId();

        if (!comment.getUser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You can only delete your own comments"
            );
        }
    }

    private void notifyOtherParticipant(Task task, User currentUser) {
        Long recipientUserId = null;

        if (permissionChecker.isAdmin()) {
            if (task.getUser() == null) {
                throw new IllegalStateException("Task has no assigned user");
            }

            recipientUserId = task.getUser().getUserId();
        } else {
            User taskCreator = userRepository.findByEmail(task.getCreatedBy())
                    .orElse(null);

            if (taskCreator != null) {
                recipientUserId = taskCreator.getUserId();
            }
        }

        if (recipientUserId != null &&
                !recipientUserId.equals(currentUser.getUserId())) {
            notificationService.createNotification(
                    recipientUserId,
                    task.getTaskId(),
                    currentUser.getUsername()
                            + " commented on task: "
                            + task.getName()
            );
        }
    }
}
