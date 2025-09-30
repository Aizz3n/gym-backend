package com.gym_system.backend.Components.UserComponent.controller;

import com.gym_system.backend.Components.UserComponent.dto.UserRequestDTO;
import com.gym_system.backend.Components.UserComponent.dto.UserResponseDTO;
import com.gym_system.backend.Components.UserComponent.model.Role;
import com.gym_system.backend.Components.UserComponent.model.UserModel;
import com.gym_system.backend.Components.UserComponent.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO body){
        UserModel toSave = toModel(body);
        UserModel created = userService.createUser (toSave);
        URI location = URI.create(String.format("/api/users/%s", created.getId()));
        return ResponseEntity.created(location).body(toDto(created));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        List<UserResponseDTO> list = userService.getAllUsers()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id){
        UserModel user = userService.getUserById(id);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser (
            @PathVariable UUID id,
            @Valid @RequestBody UserRequestDTO body,
            @RequestHeader(value = "X-Requester-Id", required = false) UUID requesterHeader,
            @RequestParam(value = "requesterId", required = false) UUID requesterParam
    ) {
        UUID requesterId = requesterHeader != null ? requesterHeader : requesterParam;
        if (requesterId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requester id is required"
            );
        }

        UserModel toUpdate = toModel(body);
        UserModel updated = userService.updateUser (id, toUpdate, requesterId);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (
            @PathVariable UUID id,
            @RequestHeader(value = "X-Requester-Id", required = false) UUID requesterHeader,
            @RequestParam(value = "requesterId", required = false) UUID requesterParam
    ) {
        UUID requesterId = requesterHeader != null ? requesterHeader : requesterParam;
        if(requesterId == null){
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requester id is required"
            );
        }

        UserModel requester = userService.getUserById(requesterId);

        if (requester.getRole() == Role.ADMIN) {
            if (requesterId.equals(id)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Admin cannot delete their own account"
                );
            }
            userService.deleteUser (id, requesterId);
        } else {
            if (!requesterId.equals(id)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "User  can only delete their own account"
                );
            }
            userService.deleteUser (id, requesterId);
        }

        return ResponseEntity.noContent().build();
    }

    private UserModel toModel(UserRequestDTO dto){
        return UserModel.builder()
                .name(dto.getName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .role(dto.getRole())
                .build();
    }

    private UserResponseDTO toDto(UserModel m){
        return UserResponseDTO.builder()
                .id(m.getId())
                .name(m.getName())
                .lastName(m.getLastName())
                .email(m.getEmail())
                .role(m.getRole())
                .createdDate(LocalDateTime.from(m.getCreatedDate()))
                .lastModified(LocalDateTime.from(m.getLastModified()))
                .build();
    }
}
