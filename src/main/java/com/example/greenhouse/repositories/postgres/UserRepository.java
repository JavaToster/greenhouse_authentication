package com.example.greenhouse.repositories.postgres;

import com.example.greenhouse.models.user.User;
import com.example.greenhouse.util.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(long telegramId);
    boolean existsByTelegramId(long telegramId);
    long countByRole(Role role);
}
