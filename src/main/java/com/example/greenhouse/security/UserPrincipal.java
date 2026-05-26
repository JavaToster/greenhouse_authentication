package com.example.greenhouse.security;

import com.example.greenhouse.util.enums.Role;

public record UserPrincipal(Long telegramId, Role role) {
}
