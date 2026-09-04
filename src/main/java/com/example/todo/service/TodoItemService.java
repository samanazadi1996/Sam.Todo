package com.example.todo.service;

import com.example.todo.dto.TodoCreateRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.TodoUpdateRequest;

import java.util.List;

public interface TodoItemService {

    TodoResponse create(TodoCreateRequest request);

    TodoResponse update(Long id, TodoUpdateRequest request);

    void delete(Long id);

    TodoResponse getById(Long id);

    List<TodoResponse> getAll();
}
