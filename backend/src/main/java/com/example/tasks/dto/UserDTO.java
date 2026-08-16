package com.example.tasks.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private Long userId;

    private String username;

    private LocalDate birthDate;

    private Integer isInternal;

    private LocalDateTime creationDate;

    private String createdBy;

    private LocalDateTime lastUpdateDate;

    private String lastUpdatedBy;

    private String password;

    private String email;

    private String createdByFullname;

    private String roleName;

}
