package com.example.tasks.mapper;

import com.example.tasks.domain.User;
import com.example.tasks.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDto(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .birthDate(user.getBirthDate())
                .isInternal(user.getIsInternal())
                .creationDate(user.getCreationDate())
                .createdBy(user.getCreatedBy())
                .lastUpdateDate(user.getLastUpdateDate())
                .lastUpdatedBy(user.getLastUpdatedBy())
                .createdByFullname(user.getCreatedByFullName())
                .email(user.getEmail())
                .roleName(user.getRole().getRoleName())
                .build();
    }
}
