package com.example.tasks.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name ="name")
    private String name;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "body")
    private String body;

    @ManyToOne
    @JoinColumn(name = "status_type_id")
    private StatusType statusType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



}
