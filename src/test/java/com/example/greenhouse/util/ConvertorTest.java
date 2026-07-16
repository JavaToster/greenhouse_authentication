package com.example.greenhouse.util;

import com.example.greenhouse.DTO.auth.SingUpDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.models.User;
import com.example.greenhouse.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConvertorTest {

    private Convertor convertor;

    @BeforeEach
    void setUp() {
        convertor = new Convertor();
    }

    @Test
    void shouldMapSignUpDtoToUserWithTelegramIdAndEmail() {
        SingUpDTO dto = new SingUpDTO(123L, "user@example.com", "raw-password");

        User user = convertor.convertToUser(dto);

        assertEquals(123L, user.getTelegramId());
        assertEquals("user@example.com", user.getEmail());
    }

    @Test
    void shouldMapSingleUserToUserInfoDto() {
        User user = new User();
        user.setTelegramId(123L);
        user.setEmail("user@example.com");
        user.setRole(Role.ROLE_OWNER);

        UserInfoDTO dto = convertor.convertToUserInfoDTO(user);

        assertEquals(123L, dto.telegramId());
        assertEquals("user@example.com", dto.email());
        assertEquals(Role.ROLE_OWNER, dto.role());
    }

    @Test
    void shouldMapCollectionOfUsersToUserInfoDtoList() {
        User first = new User();
        first.setTelegramId(1L);
        first.setEmail("a@example.com");
        first.setRole(Role.ROLE_WORKER);

        User second = new User();
        second.setTelegramId(2L);
        second.setEmail("b@example.com");
        second.setRole(Role.ROLE_ADMIN);

        List<UserInfoDTO> result = convertor.convertToUserInfoDTO(List.of(first, second));

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).telegramId());
        assertEquals(2L, result.get(1).telegramId());
    }

    @Test
    void shouldReturnEmptyListWhenConvertingEmptyCollection() {
        List<UserInfoDTO> result = convertor.convertToUserInfoDTO(List.<User>of());

        assertTrue(result.isEmpty());
    }
}