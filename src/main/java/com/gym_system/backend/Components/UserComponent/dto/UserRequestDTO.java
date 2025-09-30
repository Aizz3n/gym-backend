package com.gym_system.backend.Components.UserComponent.dto;

import com.gym_system.backend.Components.UserComponent.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@NotBlank
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message = "Name is Required")
    private String name;

    @NotBlank(message = "Last name is Required")
    private String lastName;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is Required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private Role role;
}
