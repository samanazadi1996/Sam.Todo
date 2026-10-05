package com.example.todo.controller;

import com.example.todo.config.OpenApiConfig;
import com.example.todo.dto.RoleUpdateRequest;
import com.example.todo.dto.UserResponse;
import com.example.todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(name = "User", description = "مدیریت کاربران (فقط مدیر)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "دریافت لیست کاربران")
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "تغییر نقش‌های کاربر")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return userService.updateRoles(id, request);
    }
}
