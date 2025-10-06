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
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Validator validator;

    public UserModel createUser(UserModel userModel) {
        Set<ConstraintViolation<UserModel>> violations = validator.validate(userModel);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        String normalizedEmail = userModel.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailAndDeletedFalse(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        userModel.setEmail(normalizedEmail);

        if (userModel.getRole() == null) {
            userModel.setRole(Role.USER);
        }

        if (userModel.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create ADMIN via this endpoint");
        }

        if (userModel.getPassword() == null || userModel.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }

        userModel.setPassword(bCryptPasswordEncoder.encode(userModel.getPassword()));
        UserModel savedUser = userRepository.save(userModel);
        log.info("User created with id: {}, email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public List<UserModel> getAllUsers() {
        return userRepository.findAllByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public UserModel getUserById(UUID id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found or deleted"));
    }

    public UserModel updateUser(UUID id, UserModel updatedUser, UUID requesterId) {
        UserModel existingUser = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found or deleted"));

        UserModel requester = getRequesterOrThrow(requesterId);

        if (requester.getRole() != Role.ADMIN && !Objects.equals(requester.getId(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this user");
        }

        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
            String normalizedEmail = updatedUser.getEmail().trim().toLowerCase();
            if (!Objects.equals(existingUser.getEmail(), normalizedEmail)) {
                if (userRepository.existsByEmailAndDeletedFalse(normalizedEmail)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                }
                existingUser.setEmail(normalizedEmail);
            }
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            if (updatedUser.getPassword().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
            }
            existingUser.setPassword(bCryptPasswordEncoder.encode(updatedUser.getPassword()));
        }

        UserModel savedUser = userRepository.save(existingUser);
        log.info("User updated with id: {} by requester: {}", savedUser.getId(), requesterId);
        return savedUser;
    }

    public void deleteUser(UUID id, UUID requesterId) {
        UserModel userToDelete = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found or deleted"));

        UserModel requester = getRequesterOrThrow(requesterId);

        if (requester.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete users");
        }

        userRepository.delete(userToDelete); // soft delete via @SQLDelete na entidade
        log.info("User soft deleted with id: {} by requester: {}", id, requesterId);
    }

    private UserModel getRequesterOrThrow(UUID requesterId) {
        return userRepository.findByIdAndDeletedFalse(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Requester user not found or inactive"));
    }
}
