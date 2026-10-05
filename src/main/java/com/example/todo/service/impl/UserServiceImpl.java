package com.example.todo.service.impl;

import com.example.todo.dto.RoleUpdateRequest;
import com.example.todo.dto.UserResponse;
import com.example.todo.entity.AppUser;
import com.example.todo.entity.Role;
import com.example.todo.exception.ResourceNotFoundException;
import com.example.todo.repository.AppUserRepository;
import com.example.todo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository repository;

    public UserServiceImpl(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return repository.findAll().stream()
                .map(UserResponse::new)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateRoles(Long id, RoleUpdateRequest request) {
        AppUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر با شناسه " + id + " یافت نشد"));

        user.setRoles(request.getRoles().isEmpty()
                ? EnumSet.noneOf(Role.class)
                : EnumSet.copyOf(request.getRoles()));

        return new UserResponse(repository.save(user));
    }
}
