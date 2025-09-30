package com.gym_system.backend.Components.UserComponent.repository;

import com.gym_system.backend.Components.UserComponent.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {

    Optional<UserModel> findByEmailAndDeletedFalse(String email);
    boolean existsByEmailAndDeletedFalse(String email);
    List<UserModel> findAllByDeletedFalse();
    Page<UserModel> findAllByDeletedFalse(Pageable pageable);
    Optional<UserModel> findByIdAndDeletedFalse(UUID id);
}
