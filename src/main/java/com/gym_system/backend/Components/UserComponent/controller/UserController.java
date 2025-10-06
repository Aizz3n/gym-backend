package com.gym_system.backend.Components.UserComponent.controller;

import com.gym_system.backend.Components.UserComponent.dto.UserRequestDTO;
import com.gym_system.backend.Components.UserComponent.dto.UserResponseDTO;
import com.gym_system.backend.Components.UserComponent.mapper.Mapper;
import com.gym_system.backend.Components.UserComponent.model.UserModel;
import com.gym_system.backend.Components.UserComponent.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Mapper mapper; // Mapper injetado

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO body){
        UserModel created = userService.createUser(mapper.toModel(body));
        URI location = URI.create("/api/users/" + created.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        List<UserResponseDTO> list = userService.getAllUsers()
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id){
        UserModel user = userService.getUserById(id);
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequestDTO body,
            @AuthenticationPrincipal UserModel requester
    ) {
        if (requester == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        UserModel updated = userService.updateUser(id, mapper.toModel(body), requester.getId());
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserModel requester
    ) {
        if (requester == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        userService.deleteUser(id, requester.getId());
        return ResponseEntity.noContent().build();
    }
}