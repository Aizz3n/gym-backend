package com.gym_system.backend.Components.UserComponent.dto;

import com.gym_system.backend.Components.UserComponent.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String lastName;
    private String email;
    private Role role;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
}
