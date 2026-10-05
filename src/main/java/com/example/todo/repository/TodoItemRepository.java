package com.example.todo.repository;

import com.example.todo.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

    @Query("SELECT t FROM TodoItem t WHERE (:completed IS NULL OR t.completed = :completed) AND (:title IS NULL OR t.title LIKE %:title%)")
    List<TodoItem> findByFilters(@Param("completed") Boolean completed, @Param("title") String title);
}
