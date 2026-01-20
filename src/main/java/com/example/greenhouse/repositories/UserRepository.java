package com.example.greenhouse.repositories;

import com.example.greenhouse.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(long telegramId);
    boolean existsByTelegramId(long telegramId);
}
