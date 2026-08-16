package com.example.tasks.controller;

import com.example.tasks.dto.UserDTO;
import com.example.tasks.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@PreAuthorize("@permissionChecker.isAdmin()")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/page")
    public Page<UserDTO> getUserPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "") String search
    ) {
        return userService.getUserPage(page, size, sortBy, direction, search);
    }

    @PatchMapping("/{id}/role")
    public UserDTO updateRole(@PathVariable Long id, @RequestParam String roleName) {
        return userService.updateRole(id, roleName);
    }


    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
