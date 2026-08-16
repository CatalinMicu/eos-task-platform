package com.example.tasks.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "status_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) //sys_guid
    @Column(name = "status_type_id")
    private String statusTypeId;

    @Column(name = "status_name")
    private String statusName;

    @Column(name = "creation_date")
    @Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_update_date")
    @Builder.Default
    private LocalDateTime lastUpdateDate = LocalDateTime.now();

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @Column(name = "created_by_fullname")
    private String createdByFullName;

    @JsonIgnore
    @OneToMany(mappedBy = "statusType")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

}