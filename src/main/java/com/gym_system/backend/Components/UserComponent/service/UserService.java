package com.gym_system.backend.Components.UserComponent.service;

import com.gym_system.backend.Components.UserComponent.model.Role;
import com.gym_system.backend.Components.UserComponent.model.UserModel;
import com.gym_system.backend.Components.UserComponent.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public UserModel createUser(UserModel userModel) {
        if (userRepository.existsByEmailAndDeletedFalse(userModel.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        userModel.setPassword(bCryptPasswordEncoder.encode(userModel.getPassword()));
        return userRepository.save(userModel);
    }

    @Transactional(readOnly = true)
    public List<UserModel> getAllUsers() {
        return userRepository.findAllByDeletedFalse();
    }

    public UserModel getUserById(UUID id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public void deleteUser(UUID id, UUID requesterId) {
        UserModel userToDelete = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserModel requester = userRepository.findByIdAndDeletedFalse(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Requester not found"));

        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete users");
        }

        userToDelete.setDeleted(true);
        userRepository.save(userToDelete);
    }
}
