package com.example.tasks.repository;

import com.example.tasks.domain.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCommentRepository
        extends JpaRepository<TaskComment, Long> {

    List<TaskComment>
    findAllByTask_TaskIdOrderByCreationDateAsc(Long taskId);

    Optional<TaskComment> findByCommentIdAndTask_TaskId(
            Long commentId,
            Long taskId
    );

    void deleteAllByTask_TaskId(Long taskId);

    void deleteAllByUser_UserId(Long userId);
}
