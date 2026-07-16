package com.example.greenhouse.services;

import com.example.greenhouse.DTO.auth.SingInDTO;
import com.example.greenhouse.DTO.auth.SingUpDTO;
import com.example.greenhouse.DTO.auth.SuccessfullyAuthenticatedDTO;
import com.example.greenhouse.DTO.user.AssignRoleToPersonDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.exceptions.auth.UserAlreadyExistException;
import com.example.greenhouse.models.User;
import com.example.greenhouse.security.jwt.JwtAuthenticationProvider;
import com.example.greenhouse.store.UserStore;
import com.example.greenhouse.util.Convertor;
import com.example.greenhouse.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserStore userStore;

    @Mock
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Mock
    private Convertor convertor;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserInfoWhenTelegramIdExists() {
        long telegramId = 123L;
        User user = new User();
        user.setTelegramId(telegramId);
        UserInfoDTO expectedDto = new UserInfoDTO(telegramId, "user@example.com", Role.ROLE_OWNER);

        when(userStore.findById(telegramId)).thenReturn(user);
        when(convertor.convertToUserInfoDTO(user)).thenReturn(expectedDto);

        UserInfoDTO result = userService.findUserByTelegramId(telegramId);

        assertEquals(expectedDto, result);
        verify(userStore, times(1)).findById(telegramId);
    }

    @Test
    void shouldSignUpUserSuccessfullyWhenUserDoesNotExist() {
        SingUpDTO signUpDTO = new SingUpDTO(123L, "user@example.com", "raw-password");
        User newUser = new User();
        newUser.setTelegramId(123L);
        newUser.setEmail("user@example.com");

        when(userStore.exist(123L, "user@example.com")).thenReturn(false);
        when(convertor.convertToUser(signUpDTO)).thenReturn(newUser);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(jwtAuthenticationProvider.generate(123L, Role.ROLE_UNKNOWN)).thenReturn("jwt-token");

        SuccessfullyAuthenticatedDTO result = userService.singUp(signUpDTO);

        assertEquals("jwt-token", result.jwt());
        assertEquals(Role.ROLE_UNKNOWN, newUser.getRole());
        assertEquals("encoded-password", newUser.getPassword());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userStore, times(1)).save(userCaptor.capture());
        assertSame(newUser, userCaptor.getValue());
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExistsOnSignUp() {
        SingUpDTO signUpDTO = new SingUpDTO(123L, "user@example.com", "raw-password");
        when(userStore.exist(123L, "user@example.com")).thenReturn(true);

        UserAlreadyExistException exception = assertThrows(UserAlreadyExistException.class,
                () -> userService.singUp(signUpDTO));
        assertEquals("User already exists", exception.getMessage());

        verify(userStore, never()).save(any());
        verify(jwtAuthenticationProvider, never()).generate(anyLong(), any());
    }

    @Test
    void shouldSignInSuccessfullyWhenCredentialsAreValid() {
        SingInDTO signInDTO = new SingInDTO(123L, "raw-password");
        User existingUser = new User();
        existingUser.setTelegramId(123L);
        existingUser.setPassword("encoded-password");
        existingUser.setRole(Role.ROLE_OWNER);

        when(userStore.findById(123L)).thenReturn(existingUser);
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(jwtAuthenticationProvider.generate(123L, Role.ROLE_OWNER)).thenReturn("jwt-token");

        SuccessfullyAuthenticatedDTO result = userService.singIn(signInDTO);

        assertEquals("jwt-token", result.jwt());
        verify(jwtAuthenticationProvider, times(1)).generate(123L, Role.ROLE_OWNER);
    }

    @Test
    void shouldNotAuthenticateWhenPasswordIsWrong() {
        SingInDTO signInDTO = new SingInDTO(123L, "wrong-password");
        User existingUser = new User();
        existingUser.setTelegramId(123L);
        existingUser.setPassword("encoded-password");
        existingUser.setRole(Role.ROLE_OWNER);

        when(userStore.findById(123L)).thenReturn(existingUser);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> userService.singIn(signInDTO));
        assertEquals("Invalid login or password", exception.getMessage());

        verify(jwtAuthenticationProvider, never()).generate(anyLong(), any());
    }

    @Test
    void shouldUpdateUserRoleSuccessfully() {
        long userId = 123L;
        User existingUser = new User();
        existingUser.setTelegramId(userId);
        existingUser.setRole(Role.ROLE_UNKNOWN);
        AssignRoleToPersonDTO dto = new AssignRoleToPersonDTO("ROLE_ADMIN");

        when(userStore.findById(userId)).thenReturn(existingUser);

        userService.setRoleOfUser(userId, dto);

        assertEquals(Role.ROLE_ADMIN, existingUser.getRole());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userStore, times(1)).save(userCaptor.capture());
        assertEquals(Role.ROLE_ADMIN, userCaptor.getValue().getRole());
    }

    @Test
    void shouldThrowExceptionWhenAssigningInvalidRoleString() {
        long userId = 123L;
        User existingUser = new User();
        existingUser.setTelegramId(userId);
        AssignRoleToPersonDTO dto = new AssignRoleToPersonDTO("NOT_A_REAL_ROLE");

        when(userStore.findById(userId)).thenReturn(existingUser);

        assertThrows(IllegalArgumentException.class, () -> userService.setRoleOfUser(userId, dto));

        verify(userStore, never()).save(any());
    }

    @Test
    void shouldRemoveUserById() {
        userService.remove(123L);

        verify(userStore, times(1)).remove(123L);
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = List.of(new User(), new User());
        List<UserInfoDTO> expected = List.of(
                new UserInfoDTO(1L, "a@example.com", Role.ROLE_WORKER),
                new UserInfoDTO(2L, "b@example.com", Role.ROLE_OWNER)
        );

        when(userStore.findAll()).thenReturn(users);
        when(convertor.convertToUserInfoDTO(users)).thenReturn(expected);

        List<UserInfoDTO> result = userService.findAllUsers();

        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() {
        when(userStore.findAll()).thenReturn(List.of());
        when(convertor.convertToUserInfoDTO(List.<User>of())).thenReturn(List.of());

        List<UserInfoDTO> result = userService.findAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnUsersByGivenIds() {
        Set<Long> ids = Set.of(1L, 2L);
        List<User> users = List.of(new User(), new User());
        List<UserInfoDTO> expected = List.of(
                new UserInfoDTO(1L, "a@example.com", Role.ROLE_WORKER),
                new UserInfoDTO(2L, "b@example.com", Role.ROLE_OWNER)
        );

        when(userStore.findByIds(ids)).thenReturn(users);
        when(convertor.convertToUserInfoDTO(users)).thenReturn(expected);

        List<UserInfoDTO> result = userService.findUsersById(ids);

        assertEquals(expected, result);
        verify(userStore, times(1)).findByIds(ids);
    }
}