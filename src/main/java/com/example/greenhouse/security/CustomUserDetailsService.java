package com.example.greenhouse.security;


import com.example.greenhouse.models.user.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface CustomUserDetailsService extends UserDetailsService {
    User findUserByTelegramId(long telegramId);
}
