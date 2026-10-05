package com.example.todo.entity;

public enum Role {

    USER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
