package com.example.todo.dto;

import com.example.todo.entity.Role;

import java.time.Instant;
import java.util.Set;

public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private Instant issuedAt;
    private String username;
    private Set<Role> roles;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String tokenType, long expiresInSeconds, Instant issuedAt,
                        String username, Set<Role> roles) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.issuedAt = issuedAt;
        this.username = username;
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
