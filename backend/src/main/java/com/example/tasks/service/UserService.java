package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.Roles;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.UserDTO;
import com.example.tasks.mapper.UserMapper;
import com.example.tasks.repository.NotificationsRepository;
import com.example.tasks.repository.RoleRepository;
import com.example.tasks.repository.TaskCommentRepository;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final NotificationsRepository notificationsRepository;
    private final UserMapper userMapper;
    private final PermissionChecker permissionChecker;


    public List<UserDTO> getAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            users.add(userMapper.toDto(user));
        }

        return users;
    }

    public Page<UserDTO> getUserPage(
            int page,
            int size,
            String sortBy,
            String direction,
            String search
    ) {
        String sortField = getUserSortField(sortBy);
        Sort sort = Sort.by(sortField).ascending();
        if ("desc".equalsIgnoreCase(direction)) {
            sort = Sort.by(sortField).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage;

        if (search == null || search.isBlank()) {
            userPage = userRepository.findAll(pageable);
        } else {
            String searchValue = search.trim();
            userPage = userRepository
                    .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            searchValue,
                            searchValue,
                            pageable
                    );
        }

        List<UserDTO> userDTOs = new ArrayList<>();

        for (User user : userPage.getContent()) {
            userDTOs.add(userMapper.toDto(user));
        }

        return new PageImpl<>(
                userDTOs,
                pageable,
                userPage.getTotalElements()
        );
    }

    @Transactional
    public void deleteUser(Long id) {
        requireUserPermission("DELETE");

        User currentUser = permissionChecker.getCurrentUser();
        User userToDelete = userRepository.findById(id).orElse(null);

        if (userToDelete == null) {
            log.warn("User not found with id {}", id);
            return;
        }
        if (currentUser.getUserId().equals(id)) {
            throw new AccessDeniedException("You cannot delete your own account");
        }

        requireAdminManagementPermission(userToDelete);

        List<Task> assignedTasks = taskRepository.findAllByUser_UserId(id);

        for (Task task : assignedTasks) {
            notificationsRepository.deleteAllByTaskId(task.getTaskId());
            taskCommentRepository.deleteAllByTask_TaskId(task.getTaskId());
        }

        notificationsRepository.deleteAllByUserId(id);
        taskCommentRepository.deleteAllByUser_UserId(id);
        taskRepository.deleteAll(assignedTasks);
        userRepository.delete(userToDelete);
        log.info("User deleted with id {}", id);
    }

    public UserDTO updateRole(Long id, String roleName) {
        requireUserPermission("UPDATE_ROLE");

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            log.warn("User not found with id {}", id);
            return null;
        }

        requireAdminManagementPermission(user);

        if ("SUPER_ADMIN".equalsIgnoreCase(roleName)) {
            throw new AccessDeniedException(
                    "SUPER_ADMIN role cannot be assigned from the application"
            );
        }

        Roles role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role " + roleName + " not found"));
        user.setRole(role);
        return userMapper.toDto(userRepository.save(user));
    }

    private void requireUserPermission(String action) {
        if (!permissionChecker.hasPermission("USER", action)) {
            throw new AccessDeniedException(
                    "Missing " + action + " permission on USER"
            );
        }
    }

    private void requireAdminManagementPermission(User user) {
        String roleName = user.getRole().getRoleName();

        if ("SUPER_ADMIN".equalsIgnoreCase(roleName)) {
            throw new AccessDeniedException(
                    "A SUPER_ADMIN account cannot be changed"
            );
        }

        if ("ADMIN".equalsIgnoreCase(roleName) &&
                !permissionChecker.hasPermission("USER", "MANAGE_ADMIN")) {
            throw new AccessDeniedException(
                    "Only SUPER_ADMIN can manage ADMIN accounts"
            );
        }
    }

    private String getUserSortField(String sortBy) {
        if ("id".equalsIgnoreCase(sortBy)) {
            return "userId";
        }
        if ("email".equalsIgnoreCase(sortBy)) {
            return "email";
        }
        if ("role".equalsIgnoreCase(sortBy)) {
            return "role.roleName";
        }

        return "username";
    }
}
