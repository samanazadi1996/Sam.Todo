package com.example.todo.service;

import com.example.todo.dto.TodoCreateRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.TodoUpdateRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TodoItemService {

    TodoResponse create(TodoCreateRequest request);

    TodoResponse update(Long id, TodoUpdateRequest request);

    void delete(Long id);

    TodoResponse getById(Long id);

    Page<TodoResponse> getPageable(Boolean completed,
                                   String title,
                                   LocalDateTime createdAtFrom,
                                   LocalDateTime createdAtTo,
                                   Pageable pageable);
}
