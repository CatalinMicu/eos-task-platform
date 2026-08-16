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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskCommentMapper taskCommentMapper;

    @Mock
    private PermissionChecker permissionChecker;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskCommentService taskCommentService;

    @Test
    void getCommentsIsRejectedWhenUserCannotAccessTask() {
        Task task = Task.builder()
                .taskId(10L)
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(permissionChecker.canAccessTask(task, "READ")).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> taskCommentService.getComments(10L)
        );

        verify(taskCommentRepository, never())
                .findAllByTask_TaskIdOrderByCreationDateAsc(10L);
    }

    @Test
    void addCommentUsesCurrentUserAndNotifiesTaskCreator() {
        User currentUser = User.builder()
                .userId(2L)
                .username("ana")
                .build();
        User taskCreator = User.builder()
                .userId(1L)
                .email("admin@example.com")
                .build();
        Task task = Task.builder()
                .taskId(10L)
                .name("Prepare report")
                .user(currentUser)
                .createdBy("admin@example.com")
                .build();
        TaskCommentDTO request = TaskCommentDTO.builder()
                .body("The report is ready")
                .build();
        TaskComment savedComment = TaskComment.builder()
                .commentId(100L)
                .task(task)
                .user(currentUser)
                .body("The report is ready")
                .build();
        TaskCommentDTO response = TaskCommentDTO.builder()
                .commentId(100L)
                .taskId(10L)
                .userId(2L)
                .body("The report is ready")
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(permissionChecker.canAccessTask(task, "READ")).thenReturn(true);
        when(permissionChecker.getCurrentUser()).thenReturn(currentUser);
        when(taskCommentRepository.save(any(TaskComment.class)))
                .thenReturn(savedComment);
        when(permissionChecker.isAdmin()).thenReturn(false);
        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(taskCreator));
        when(taskCommentMapper.toDto(savedComment)).thenReturn(response);

        TaskCommentDTO result = taskCommentService.addComment(10L, request);

        ArgumentCaptor<TaskComment> commentCaptor =
                ArgumentCaptor.forClass(TaskComment.class);
        verify(taskCommentRepository).save(commentCaptor.capture());

        TaskComment commentToSave = commentCaptor.getValue();
        assertSame(response, result);
        assertSame(task, commentToSave.getTask());
        assertSame(currentUser, commentToSave.getUser());
        assertEquals("The report is ready", commentToSave.getBody());
        verify(notificationService).createNotification(
                1L,
                10L,
                "ana commented on task: Prepare report"
        );
    }

    @Test
    void deleteCommentIsRejectedForDifferentAuthor() {
        User currentUser = User.builder()
                .userId(2L)
                .build();
        User commentAuthor = User.builder()
                .userId(3L)
                .build();
        Task task = Task.builder()
                .taskId(10L)
                .build();
        TaskComment comment = TaskComment.builder()
                .commentId(100L)
                .task(task)
                .user(commentAuthor)
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(permissionChecker.canAccessTask(task, "READ")).thenReturn(true);
        when(taskCommentRepository.findByCommentIdAndTask_TaskId(100L, 10L))
                .thenReturn(Optional.of(comment));
        when(permissionChecker.getCurrentUser()).thenReturn(currentUser);

        assertThrows(
                AccessDeniedException.class,
                () -> taskCommentService.deleteComment(10L, 100L)
        );

        verify(taskCommentRepository, never()).delete(any(TaskComment.class));
    }
}
