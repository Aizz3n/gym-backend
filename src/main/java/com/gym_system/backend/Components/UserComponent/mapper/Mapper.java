package com.gym_system.backend.Components.UserComponent.mapper;

import com.gym_system.backend.Components.UserComponent.dto.UserRequestDTO;
import com.gym_system.backend.Components.UserComponent.dto.UserResponseDTO;
import com.gym_system.backend.Components.UserComponent.model.UserModel;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class Mapper {

    public UserModel toModel(UserRequestDTO dto) {
        if (dto == null) return null;
        return UserModel.builder()
                .name(dto.getName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();
    }

    public UserResponseDTO toResponse(UserModel model) {
        if (model == null) return null;
        return UserResponseDTO.builder()
                .id(model.getId())
                .name(model.getName())
                .lastName(model.getLastName())
                .email(model.getEmail())
                .role(model.getRole())
                .createdDate(model.getCreatedDate().atZone(ZoneOffset.UTC).toLocalDateTime())
                .lastModified(model.getLastModified().atZone(ZoneOffset.UTC).toLocalDateTime())
                .build();
    }
}