package com.example.tasks.mapper;

import com.example.tasks.domain.StatusType;
import com.example.tasks.dto.StatusTypeDTO;
import org.springframework.stereotype.Component;

@Component
public class StatusTypeMapper {
    public StatusTypeDTO toDto(StatusType statusType) {
        return StatusTypeDTO.builder()
                .statusTypeId(statusType.getStatusTypeId())
                .statusName(statusType.getStatusName())
                .creationDate(statusType.getCreationDate())
                .createdBy(statusType.getCreatedBy())
                .lastUpdateDate(statusType.getLastUpdateDate())
                .lastUpdatedBy(statusType.getLastUpdatedBy())
                .createdByFullname(statusType.getCreatedByFullName())
                .build();
    }
}
