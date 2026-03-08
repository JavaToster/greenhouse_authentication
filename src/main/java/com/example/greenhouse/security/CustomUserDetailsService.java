package com.example.greenhouse.security;


import com.example.greenhouse.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {
    User findUserByTelegramId(long telegramId);
}
