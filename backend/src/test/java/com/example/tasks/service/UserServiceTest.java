package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.Roles;
import com.example.tasks.domain.User;
import com.example.tasks.dto.UserDTO;
import com.example.tasks.mapper.UserMapper;
import com.example.tasks.repository.NotificationsRepository;
import com.example.tasks.repository.RoleRepository;
import com.example.tasks.repository.TaskCommentRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private NotificationsRepository notificationsRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PermissionChecker permissionChecker;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserPageSearchesByUsernameOrEmail() {
        User user = User.builder()
                .userId(2L)
                .username("ana")
                .email("ana@example.com")
                .build();
        UserDTO userDTO = UserDTO.builder()
                .userId(2L)
                .username("ana")
                .email("ana@example.com")
                .build();

        when(userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        eq("ana"),
                        eq("ana"),
                        any(Pageable.class)
                ))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toDto(user)).thenReturn(userDTO);

        Page<UserDTO> result = userService.getUserPage(
                0,
                8,
                "username",
                "asc",
                "  ana  "
        );

        assertEquals(1, result.getTotalElements());
        assertSame(userDTO, result.getContent().getFirst());
        verify(userRepository)
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        eq("ana"),
                        eq("ana"),
                        any(Pageable.class)
                );
    }

    @Test
    void deleteUserRejectsDeletingOwnAccount() {
        User currentUser = User.builder()
                .userId(1L)
                .build();

        when(permissionChecker.hasPermission("USER", "DELETE")).thenReturn(true);
        when(permissionChecker.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.deleteUser(1L)
        );

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateRoleRejectsAdminTargetForNormalAdmin() {
        Roles adminRole = Roles.builder()
                .roleName("ADMIN")
                .build();
        User targetAdmin = User.builder()
                .userId(2L)
                .role(adminRole)
                .build();

        when(permissionChecker.hasPermission("USER", "UPDATE_ROLE"))
                .thenReturn(true);
        when(permissionChecker.hasPermission("USER", "MANAGE_ADMIN"))
                .thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAdmin));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateRole(2L, "USER")
        );

        verify(roleRepository, never()).findByRoleName(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRoleChangesRoleForRegularUser() {
        Roles userRole = Roles.builder()
                .roleName("USER")
                .build();
        Roles adminRole = Roles.builder()
                .roleName("ADMIN")
                .build();
        User targetUser = User.builder()
                .userId(2L)
                .role(userRole)
                .build();
        UserDTO response = UserDTO.builder()
                .userId(2L)
                .roleName("ADMIN")
                .build();

        when(permissionChecker.hasPermission("USER", "UPDATE_ROLE"))
                .thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(targetUser)).thenReturn(targetUser);
        when(userMapper.toDto(targetUser)).thenReturn(response);

        UserDTO result = userService.updateRole(2L, "ADMIN");

        assertSame(response, result);
        assertSame(adminRole, targetUser.getRole());
        verify(userRepository).save(targetUser);
    }
}
