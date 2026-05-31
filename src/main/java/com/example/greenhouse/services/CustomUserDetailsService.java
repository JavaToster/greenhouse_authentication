package com.example.greenhouse.services;


import com.example.greenhouse.DTO.user.UserInfoDTO;

public interface CustomUserDetailsService{
    UserInfoDTO findUserByTelegramId(long telegramId);
}
