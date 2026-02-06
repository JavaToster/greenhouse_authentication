package com.example.greenhouse.DAO.user;

import com.example.greenhouse.models.user.User;
import com.example.greenhouse.repositories.postgres.UserRepository;
import com.example.greenhouse.util.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDAO {
    private final UserRepository userRepository;

    public long count(){
        return userRepository.count();
    }

    public long count(Role role){
        return userRepository.countByRole(role);
    }

    public User find(long telegramId){
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователя с таким id не существует"));
    }

    public boolean exist(long telegramId){
        return userRepository.existsByTelegramId(telegramId);
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
}
