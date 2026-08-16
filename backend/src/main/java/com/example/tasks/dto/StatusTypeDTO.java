package com.example.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusTypeDTO {


    private String statusTypeId;

    @NotBlank
    private String statusName;

    private LocalDateTime creationDate;

    private String createdBy;

    private LocalDateTime lastUpdateDate;

    private String lastUpdatedBy;

    private String createdByFullname;
}
