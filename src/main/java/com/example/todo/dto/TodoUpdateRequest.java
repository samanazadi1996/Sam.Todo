package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TodoUpdateRequest {

    @NotBlank(message = "عنوان نمی‌تواند خالی باشد")
    @Size(max = 255, message = "عنوان حداکثر ۲۵۵ کاراکتر باشد")
    private String title;

    @Size(max = 2000, message = "توضیحات حداکثر ۲۰۰۰ کاراکتر باشد")
    private String description;

    private Boolean completed;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
