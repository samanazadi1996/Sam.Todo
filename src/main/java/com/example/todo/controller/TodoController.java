package com.example.todo.controller;

import com.example.todo.dto.TodoCreateRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.TodoUpdateRequest;
import com.example.todo.service.TodoItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
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
    @Operation(summary = "به‌روزرسانی وظیفه")
    public ResponseEntity<TodoResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TodoUpdateRequest request) {
        return ResponseEntity.ok(todoItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "حذف وظیفه")
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
    public ResponseEntity<List<TodoResponse>> getAll() {
        return ResponseEntity.ok(todoItemService.getAll());
    }
}
