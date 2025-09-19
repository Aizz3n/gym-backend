package com.gym_system.backend.Components.UserComponent.repository;

import com.gym_system.backend.Components.UserComponent.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByEmailAndDeletedFalse(String email);
    boolean existsByEmailAndDeletedFalse(String email);
    List<UserModel> findAllByDeletedFalse();
    Optional<UserModel> findByIdAndDeletedFalse(UUID id);
}
