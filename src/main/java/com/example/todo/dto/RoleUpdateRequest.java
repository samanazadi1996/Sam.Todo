package com.example.todo.dto;

import com.example.todo.entity.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class RoleUpdateRequest {

    @NotEmpty(message = "حداقل یک نقش باید انتخاب شود")
    private Set<Role> roles;

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
