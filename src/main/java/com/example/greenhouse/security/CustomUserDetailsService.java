package com.example.greenhouse.security;


import com.example.greenhouse.models.user.User;

import java.util.Optional;

public interface CustomUserDetailsService {
    User findUserByTelegramId(long telegramId);
}
