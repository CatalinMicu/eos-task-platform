package com.example.tasks.config;

import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.domain.Permissions;
import com.example.tasks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionChecker {

    private final UserRepository userRepository;


    public User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof Long userId)) {
            throw new IllegalStateException("User is not authenticated");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public boolean hasPermission(String resource, String action) {
        User user = getCurrentUser();

        for (Permissions permission : user.getRole().getPermissions()) {
            boolean sameResource =
                    permission.getResourceName().equalsIgnoreCase(resource);
            boolean sameAction =
                    permission.getPermissionAction().equalsIgnoreCase(action);

            if (sameResource && sameAction) {
                return true;
            }
        }

        return false;
    }

    public boolean isAdmin() {
        return hasAdminRole(getCurrentUser());
    }

    public boolean canAccessTask(Task task, String action) {
        User user = getCurrentUser();

        if (!hasPermission("TASK", action)) {
            return false;
        }

        if (hasAdminRole(user)) {
            return true;
        }

        boolean ownsTask =
                task.getUser() != null &&
                        task.getUser().getUserId().equals(user.getUserId());

        return ownsTask &&
                ("READ".equalsIgnoreCase(action) ||
                        "UPDATE_STATUS".equalsIgnoreCase(action));
    }

    private boolean hasAdminRole(User user) {
        String roleName = user.getRole().getRoleName();

        return "ADMIN".equalsIgnoreCase(roleName) ||
                "SUPER_ADMIN".equalsIgnoreCase(roleName);
    }
}
