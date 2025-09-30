package com.gym_system.backend.Components.UserComponent.service;

import com.gym_system.backend.Components.UserComponent.model.Role;
import com.gym_system.backend.Components.UserComponent.model.UserModel;
import com.gym_system.backend.Components.UserComponent.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Validator validator;

    public UserModel createUser (UserModel userModel) {
        Set<ConstraintViolation<UserModel>> violations = validator.validate(userModel);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (userRepository.existsByEmailAndDeletedFalse(userModel.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        userModel.setPassword(bCryptPasswordEncoder.encode(userModel.getPassword()));
        UserModel savedUser = userRepository.save(userModel);
        log.info("User  created with id: {}", savedUser .getId());
        return savedUser ;
    }

    @Transactional(readOnly = true)
    public List<UserModel> getAllUsers() {
        return userRepository.findAllByDeletedFalse();
    }

    public UserModel getUserById(UUID id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserModel updateUser (UUID id, UserModel updatedUser , UUID requesterId) {
        Set<ConstraintViolation<UserModel>> violations = validator.validate(updatedUser );
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        UserModel existingUser  = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserModel requester = userRepository.findByIdAndDeletedFalse(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Requester not found"));

        if (!requester.getRole().equals(Role.ADMIN) && !requester.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this user");
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setLastName(updatedUser.getLastName());

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmailAndDeletedFalse(updatedUser .getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
            }
            existingUser.setEmail(updatedUser .getEmail());
        }

        if (updatedUser.getPassword() != null && !updatedUser .getPassword().isBlank()) {
            existingUser.setPassword(bCryptPasswordEncoder.encode(updatedUser .getPassword()));
        }

        UserModel savedUser  = userRepository.save(existingUser );
        log.info("User updated with id: {}", savedUser.getId());
        return savedUser ;
    }

    public void deleteUser (UUID id, UUID requesterId) {
        UserModel userToDelete = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User  not found"));

        UserModel requester = userRepository.findByIdAndDeletedFalse(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Requester not found"));

        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete users");
        }

        userRepository.delete(userToDelete);
        log.info("User  soft deleted with id: {}", id);
    }
}
