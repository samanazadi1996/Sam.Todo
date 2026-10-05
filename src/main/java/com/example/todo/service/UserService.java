package com.example.todo.service;

import com.example.todo.dto.RoleUpdateRequest;
import com.example.todo.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAll();

    UserResponse updateRoles(Long id, RoleUpdateRequest request);
}
