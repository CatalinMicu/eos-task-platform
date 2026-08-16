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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final PermissionChecker permissionChecker;
    private final NotificationService notificationService;

    public Page<TaskDTO> getTaskPage(
            int page,
            int size,
            String sortBy,
            String direction,
            String view
    ) {
        requirePermission("READ");

        String sortField = getTaskSortField(sortBy);
        Sort sort = Sort.by(sortField).ascending();
        if ("desc".equalsIgnoreCase(direction)) {
            sort = Sort.by(sortField).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        User currentUser = permissionChecker.getCurrentUser();
        Page<Task> taskPage;
        boolean isAdmin = permissionChecker.isAdmin();

        if (isAdmin && "assigned".equalsIgnoreCase(view)) {
            taskPage = taskRepository.findAllByCreatedByIgnoreCase(
                    currentUser.getEmail(),
                    pageable
            );
        } else if (isAdmin && "all".equalsIgnoreCase(view)) {
            taskPage = taskRepository.findAll(pageable);
        } else {
            taskPage = taskRepository.findAllByUser_UserId(
                    currentUser.getUserId(),
                    pageable
            );
        }

        List<TaskDTO> taskDTOs = new ArrayList<>();
        for (Task task : taskPage.getContent()) {
            taskDTOs.add(taskMapper.toDto(task));
        }

        return new PageImpl<>(
                taskDTOs,
                pageable,
                taskPage.getTotalElements()
        );
    }

    public TaskDTO getTaskById(long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return null;
        }
        requireTaskAccess(task, "READ");
        return taskMapper.toDto(task);
    }

    @Transactional
    public TaskDTO addTask(TaskDTO taskDTO) {
        requirePermission("CREATE");
        Task task = taskMapper.toEntity(taskDTO, findUser(taskDTO.getUserId()), findStatusByName(taskDTO.getStatusName()));
        User currentAdmin = permissionChecker.getCurrentUser();
        task.setCreatedBy(currentAdmin.getEmail());

        Task savedTask = taskRepository.save(task);

        notificationService.createNotification(
                savedTask.getUser().getUserId(),
                savedTask.getTaskId(),
                "You have been assigned a new task: " + savedTask.getName()
        );

        return taskMapper.toDto(savedTask);
    }

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return null;
        }
        requireTaskAccess(task, "UPDATE");

        task.setName(taskDTO.getName());
        task.setDueDate(taskDTO.getDueDate());
        task.setStatusType(findStatusByName(taskDTO.getStatusName()));
        task.setUser(findUser(taskDTO.getUserId()));
        task.setBody(taskDTO.getBody());

        return taskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long id, String statusName) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return null;
        }
        requireTaskAccess(task, "UPDATE_STATUS");

        task.setStatusType(findStatusByName(statusName));
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTaskById(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return;
        }
        requireTaskAccess(task, "DELETE");
        taskRepository.delete(task);
    }

    public List<TaskDTO> searchTasks(String assignedTo, String subject, LocalDate dueDate, String status) {
        List<TaskDTO> matchingTasks = new ArrayList<>();

        for (TaskDTO task : getTasks()) {
            if (assignedTo != null && !assignedTo.isBlank()) {
                if (task.getAssignedTo() == null ||
                        !task.getAssignedTo().toLowerCase().contains(assignedTo.toLowerCase())) {
                    continue;
                }
            }

            if (subject != null && !subject.isBlank()) {
                if (task.getName() == null ||
                        !task.getName().toLowerCase().contains(subject.toLowerCase())) {
                    continue;
                }
            }

            if (dueDate != null) {
                if (task.getDueDate() == null ||
                        !task.getDueDate().equals(dueDate)) {
                    continue;
                }
            }

            if (status != null && !status.isBlank()) {
                if (task.getStatusName() == null ||
                        !task.getStatusName().equalsIgnoreCase(status)) {
                    continue;
                }
            }

            matchingTasks.add(task);
        }

        return matchingTasks;
    }

    private StatusType findStatusByName(String statusName) {
        return statusTypeRepository.findFirstByStatusNameIgnoreCase(statusName.trim())
                .orElseThrow(() -> new IllegalArgumentException("Status not found: " + statusName));
    }

    private User findUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private void requirePermission(String action) {
        if (!permissionChecker.hasPermission("TASK", action)) {
            throw new AccessDeniedException("Missing " + action + " permission on TASK");
        }
    }

    private void requireTaskAccess(Task task, String action) {
        if (!permissionChecker.canAccessTask(task, action)) {
            throw new AccessDeniedException("Access denied for this task");
        }
    }

    private String getTaskSortField(String sortBy) {
        if ("user".equalsIgnoreCase(sortBy)) {
            return "user.username";
        }
        if ("name".equalsIgnoreCase(sortBy)) {
            return "name";
        }

        return "taskId";
    }

    public List<TaskDTO> getTasks() {
        requirePermission("READ");

        List<Task> tasks;
        if (permissionChecker.isAdmin()) {
            tasks = taskRepository.findAll();
        } else {
            Long userId = permissionChecker.getCurrentUser().getUserId();
            tasks = taskRepository.findAllByUser_UserId(userId);
        }

        List<TaskDTO> taskDTOs = new ArrayList<>();
        for (Task task : tasks) {
            taskDTOs.add(taskMapper.toDto(task));
        }

        return taskDTOs;
    }
}
