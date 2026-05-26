package com.example.greenhouse.security;


import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService{
    UserInfoDTO findUserByTelegramId(long telegramId);
}
