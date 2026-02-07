package com.example.greenhouse.services;

import com.example.greenhouse.DAO.user.UserDAO;
import com.example.greenhouse.DTO.admin.AssignRoleToPersonDTO;
import com.example.greenhouse.DTO.auth.AfterRegisterDataDTO;
import com.example.greenhouse.DTO.auth.AuthenticationDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.exceptions.auth.UserAlreadyExistException;
import com.example.greenhouse.models.user.User;
import com.example.greenhouse.security.CustomUserDetailsService;
import com.example.greenhouse.security.JwtUtil;
import com.example.greenhouse.util.Convertor;
import com.example.greenhouse.util.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements CustomUserDetailsService {
    private final UserDAO userDAO;
    private final JwtUtil jwtUtil;
    private final Convertor convertor;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findUserByTelegramId(long telegramId) {
        return userDAO.find(telegramId);
    }

    @Transactional
    public AfterRegisterDataDTO singUp(AuthenticationDTO authenticationDTO) {
        log.info("Attempting to register new user with Telegram ID: {}", authenticationDTO.getTelegramId());

        if (userDAO.exist(authenticationDTO.getTelegramId())) {
            log.warn("Registration failed: User with ID {} already exists", authenticationDTO.getTelegramId());
            throw new UserAlreadyExistException("Пользователь с таким id уже существует");
        }

        User newUser = convertor.convertToUser(authenticationDTO);
        newUser.setRole(Role.ROLE_UNKNOWN);
        newUser.setPassword(passwordEncoder.encode(authenticationDTO.getPassword()));

        userDAO.save(newUser);
        log.info("User {} successfully registered with role {}", newUser.getTelegramId(), newUser.getRole());

        String jwt = jwtUtil.generateToken(authenticationDTO.getTelegramId());
        return new AfterRegisterDataDTO(jwt);
    }

    public AfterRegisterDataDTO singIn(AuthenticationDTO authenticationDTO) {
        log.info("Login attempt for user {}", authenticationDTO.getTelegramId());

        User user = userDAO.find(authenticationDTO.getTelegramId());

        if (!passwordEncoder.matches(authenticationDTO.getPassword(), user.getPassword())) {
            log.warn("Login failed for user {}: Invalid credentials", authenticationDTO.getTelegramId());
            throw new BadCredentialsException("Неверный логин или пароль!");
        }

        log.info("User {} successfully authenticated", authenticationDTO.getTelegramId());
        String jwt = jwtUtil.generateToken(authenticationDTO.getTelegramId());
        return new AfterRegisterDataDTO(jwt);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for Spring Security by username: {}", username);
        User user = userDAO.find(Long.parseLong(username));

        return new com.example.greenhouse.security.UserDetails(user);
    }

    @Transactional
    public void setRoleOfUser(long id, AssignRoleToPersonDTO assignRoleToPersonDTO) {
        log.info("Admin action: Changing role of user {} to {}", id, assignRoleToPersonDTO.getRole());

        User user = userDAO.find(id);
        Role newRole = Role.valueOf(assignRoleToPersonDTO.getRole());

        user.setRole(newRole);
        userDAO.save(user);

        log.info("Role for user {} successfully updated to {}", id, newRole);
    }

    @Transactional
    public void remove(long id) {
        log.info("Removing user with ID: {}", id);
        userDAO.remove(id);
        log.info("User {} successfully removed", id);
    }

    public List<UserInfoDTO> findAllUsers() {
        log.debug("Fetching all users from database");
        return convertor.convertToUserInfoDTO(userDAO.findAll());
    }
}