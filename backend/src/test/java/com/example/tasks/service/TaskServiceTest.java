package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.TaskDTO;
import com.example.tasks.mapper.TaskMapper;
import com.example.tasks.repository.StatusTypeRepository;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private StatusTypeRepository statusTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private PermissionChecker permissionChecker;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTaskPageReturnsOnlyCurrentUsersTasksForRegularUser() {
        User currentUser = User.builder()
                .userId(2L)
                .email("user@example.com")
                .build();
        Task task = Task.builder()
                .taskId(10L)
                .name("Assigned task")
                .user(currentUser)
                .build();
        TaskDTO taskDTO = TaskDTO.builder()
                .taskId(10L)
                .name("Assigned task")
                .build();

        when(permissionChecker.hasPermission("TASK", "READ")).thenReturn(true);
        when(permissionChecker.getCurrentUser()).thenReturn(currentUser);
        when(permissionChecker.isAdmin()).thenReturn(false);
        when(taskRepository.findAllByUser_UserId(eq(2L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        Page<TaskDTO> result = taskService.getTaskPage(
                0,
                8,
                "id",
                "asc",
                "all"
        );

        assertEquals(1, result.getTotalElements());
        assertSame(taskDTO, result.getContent().getFirst());
        verify(taskRepository).findAllByUser_UserId(eq(2L), any(Pageable.class));
        verify(taskRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void addTaskSavesTaskAndCreatesNotification() {
        User assignee = User.builder()
                .userId(2L)
                .username("user")
                .build();
        User admin = User.builder()
                .userId(1L)
                .email("admin@example.com")
                .build();
        StatusType status = StatusType.builder()
                .statusName("To Do")
                .build();
        TaskDTO request = TaskDTO.builder()
                .name("New task")
                .dueDate(LocalDate.now().plusDays(1))
                .statusName("To Do")
                .userId(2L)
                .body("Task details")
                .build();
        Task task = Task.builder()
                .name("New task")
                .user(assignee)
                .statusType(status)
                .build();
        Task savedTask = Task.builder()
                .taskId(10L)
                .name("New task")
                .user(assignee)
                .statusType(status)
                .createdBy("admin@example.com")
                .build();
        TaskDTO response = TaskDTO.builder()
                .taskId(10L)
                .name("New task")
                .build();

        when(permissionChecker.hasPermission("TASK", "CREATE")).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(statusTypeRepository.findFirstByStatusNameIgnoreCase("To Do"))
                .thenReturn(Optional.of(status));
        when(taskMapper.toEntity(request, assignee, status)).thenReturn(task);
        when(permissionChecker.getCurrentUser()).thenReturn(admin);
        when(taskRepository.save(task)).thenReturn(savedTask);
        when(taskMapper.toDto(savedTask)).thenReturn(response);

        TaskDTO result = taskService.addTask(request);

        assertSame(response, result);
        assertEquals("admin@example.com", task.getCreatedBy());
        verify(taskRepository).save(task);
        verify(notificationService).createNotification(
                2L,
                10L,
                "You have been assigned a new task: New task"
        );
    }

    @Test
    void updateTaskStatusIsRejectedWhenUserCannotAccessTask() {
        Task task = Task.builder()
                .taskId(10L)
                .name("Protected task")
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(permissionChecker.canAccessTask(task, "UPDATE_STATUS"))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.updateTaskStatus(10L, "Done")
        );

        verify(taskRepository, never()).save(any(Task.class));
    }
}
