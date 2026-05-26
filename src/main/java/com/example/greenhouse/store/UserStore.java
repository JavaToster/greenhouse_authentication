package com.example.greenhouse.store;

import com.example.greenhouse.models.User;
import com.example.greenhouse.repositories.postgres.UserRepository;
import com.example.greenhouse.util.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserStore implements GenericStore<User, Long> {
    private final UserRepository userRepository;

    public long count(){
        return userRepository.count();
    }

    public long count(Role role){
        return userRepository.countByRole(role);
    }

    public boolean exist(long telegramId, String email){
        return userRepository.existsByTelegramIdOrEmail(telegramId, email);
    }

    @Override
    public User findById(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователя с таким id не существует"));
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public void remove(long telegramId){
        userRepository.deleteById(telegramId);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public boolean isWorkerInCluster(long workerId, UUID clusterId){
        return userRepository.isWorkerInCluster(workerId, clusterId);
    }

    public List<User> findByIds(Set<Long> ids) {
        return userRepository.findByTelegramIdIn(ids);
    }
}
