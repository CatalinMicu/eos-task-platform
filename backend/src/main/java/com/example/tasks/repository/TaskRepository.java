package com.example.tasks.repository;

import com.example.tasks.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUser_UserId(Long userId);

    Page<Task> findAllByUser_UserId(Long userId, Pageable pageable);

    Page<Task> findAllByCreatedByIgnoreCase(
            String createdBy,
            Pageable pageable
    );

}
