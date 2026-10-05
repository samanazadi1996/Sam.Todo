package com.example.todo.service.impl;

import com.example.todo.dto.AuthResponse;
import com.example.todo.dto.LoginRequest;
import com.example.todo.dto.RegisterRequest;
import com.example.todo.dto.UserResponse;
import com.example.todo.entity.AppUser;
import com.example.todo.entity.Role;
import com.example.todo.exception.ResourceNotFoundException;
import com.example.todo.exception.UsernameAlreadyExistsException;
import com.example.todo.repository.AppUserRepository;
import com.example.todo.security.JwtService;
import com.example.todo.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(AppUserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("نام کاربری " + request.getUsername() + " قبلاً ثبت شده است");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(EnumSet.of(Role.USER));
        user.setEnabled(Boolean.TRUE);

        AppUser saved = repository.save(user);
        return buildAuthResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        AppUser user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("کاربری با نام " + request.getUsername() + " یافت نشد"));

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        AppUser user = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("کاربری با نام " + username + " یافت نشد"));
        return new UserResponse(user);
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        Set<Role> roles = user.getRoles().isEmpty()
                ? EnumSet.noneOf(Role.class)
                : EnumSet.copyOf(user.getRoles());
        Instant issuedAt = Instant.now();

        return new AuthResponse(
                jwtService.generateToken(user),
                TOKEN_TYPE,
                jwtService.getExpirationMillis() / 1000,
                issuedAt,
                user.getUsername(),
                roles);
    }
}
