package com.example.greenhouse.DTO.user;

import com.example.greenhouse.util.enums.Role;
import lombok.Data;

@Data
public class UserInfoDTO {
    private long telegramId;
    private String email;
    private Role role;
}
