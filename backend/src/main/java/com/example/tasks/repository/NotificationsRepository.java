package com.example.tasks.repository;

import com.example.tasks.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationsRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdOrderByCreationDateDesc(Long userId);

    Optional<Notification>  findByNotificationIdAndUserId(
            Long notificationId,
            Long userId
    );

    void deleteAllByTaskId(Long taskId);

    void deleteAllByUserId(Long userId);

}
