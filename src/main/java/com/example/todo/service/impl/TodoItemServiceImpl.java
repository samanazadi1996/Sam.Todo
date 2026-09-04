package com.example.todo.service.impl;

import com.example.todo.dto.TodoCreateRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.TodoUpdateRequest;
import com.example.todo.entity.TodoItem;
import com.example.todo.exception.ResourceNotFoundException;
import com.example.todo.repository.TodoItemRepository;
import com.example.todo.service.TodoItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoItemServiceImpl implements TodoItemService {

    private final TodoItemRepository repository;

    public TodoItemServiceImpl(TodoItemRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TodoResponse create(TodoCreateRequest request) {
        TodoItem item = new TodoItem();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCompleted(request.isCompleted());
        return new TodoResponse(repository.save(item));
    }

    @Override
    @Transactional
    public TodoResponse update(Long id, TodoUpdateRequest request) {
        TodoItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo با شناسه " + id + " یافت نشد"));
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCompleted(request.isCompleted());
        return new TodoResponse(repository.save(item));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TodoItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo با شناسه " + id + " یافت نشد"));
        repository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public TodoResponse getById(Long id) {
        TodoItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo با شناسه " + id + " یافت نشد"));
        return new TodoResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getAll() {
        return repository.findAll().stream()
                .map(TodoResponse::new)
                .toList();
    }
}
