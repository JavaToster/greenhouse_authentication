package com.example.greenhouse.repositories.postgres;

import com.example.greenhouse.models.User;
import com.example.greenhouse.util.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(long telegramId);
    boolean existsByTelegramIdOrEmail(long telegramId, String email);
    List<User> findByTelegramIdIn(Set<Long> ids);
}
