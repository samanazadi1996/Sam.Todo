package com.example.todo.repository;

import com.example.todo.entity.TodoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

    @Query("""
    SELECT t FROM TodoItem t
    WHERE (:completed IS NULL OR t.completed = :completed)
      AND (:title IS NULL OR t.title LIKE CONCAT('%', :title, '%'))
      AND (:createdAtFrom IS NULL OR t.createdAt >= :createdAtFrom)
      AND (:createdAtTo IS NULL OR t.createdAt <= :createdAtTo)
    """)
    Page<TodoItem> findAll(
            @Param("completed") Boolean completed,
            @Param("title") String title,
            @Param("createdAtFrom") LocalDateTime createdAtFrom,
            @Param("createdAtTo") LocalDateTime createdAtTo,
            Pageable pageable
    );
}
