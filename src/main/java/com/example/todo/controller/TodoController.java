package com.example.todo.controller;

import com.example.todo.config.OpenApiConfig;
import com.example.todo.dto.TodoCreateRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.TodoUpdateRequest;
import com.example.todo.service.TodoItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(name = "Todo", description = "مدیریت وظایف (CRUD)")
public class TodoController {

    private final TodoItemService todoItemService;

    public TodoController(TodoItemService todoItemService) {
        this.todoItemService = todoItemService;
    }

    @PostMapping
    @Operation(summary = "ایجاد وظیفه جدید")
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoCreateRequest request) {
        TodoResponse response = todoItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "به‌روزرسانی وظیفه (فقط مدیر)")
    public ResponseEntity<TodoResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TodoUpdateRequest request) {
        return ResponseEntity.ok(todoItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "حذف وظیفه (فقط مدیر)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "دریافت یک وظیفه بر اساس شناسه")
    public ResponseEntity<TodoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(todoItemService.getById(id));
    }

    @GetMapping
    @Operation(summary = "دریافت همه وظایف")
    public ResponseEntity<List<TodoResponse>> getAll(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String title) {
        return ResponseEntity.ok(todoItemService.getAll(completed, title));
    }
}
