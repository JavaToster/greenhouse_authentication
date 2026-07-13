package com.example.greenhouse.util;

import com.example.greenhouse.DTO.auth.SingUpDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.models.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class Convertor {
    public User convertToUser(SingUpDTO authenticationDTO){
        User user = new User();
        user.setTelegramId(authenticationDTO.telegramId());
        user.setEmail(authenticationDTO.email());
        user.setPassword(authenticationDTO.password());
        return user;
    }

    public List<UserInfoDTO> convertToUserInfoDTO(Collection<? extends User> all) {
        return all.stream()
                .map(this::convertToUserInfoDTO)
                .toList();
    }

    public UserInfoDTO convertToUserInfoDTO(User user){
        return new UserInfoDTO(user.getTelegramId(), user.getEmail(), user.getRole());
    }
}
