package com.example.greenhouse.repositories.postgres;

import com.example.greenhouse.models.User;
import com.example.greenhouse.util.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(long telegramId);
    boolean existsByTelegramIdOrEmail(long telegramId, String email);
    long countByRole(Role role);
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END "+
            "FROM User u JOIN u.clustersToWork c " +
            "WHERE u.telegramId = :userId AND c.id = :clusterId"
    )
    boolean isWorkerInCluster(@Param("userId") long workerId, @Param("clusterId") UUID clusterId);
}
