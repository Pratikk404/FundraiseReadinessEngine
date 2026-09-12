package com.fundraise.engine.repository;

import com.fundraise.engine.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(User.Role role);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByResetToken(String token);
}
