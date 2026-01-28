package com.example.greenhouse.services;

import com.example.greenhouse.DTO.auth.AfterRegisterDataDTO;
import com.example.greenhouse.DTO.auth.AuthenticationDTO;
import com.example.greenhouse.exceptions.auth.UserAlreadyExistException;
import com.example.greenhouse.models.user.User;
import com.example.greenhouse.repositories.postgres.UserRepository;
import com.example.greenhouse.security.CustomUserDetailsService;
import com.example.greenhouse.security.JwtUtil;
import com.example.greenhouse.util.Convertor;
import com.example.greenhouse.util.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements CustomUserDetailsService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Convertor convertor;
    private final PasswordEncoder passwordEncoder;
    @Override
    public User findUserByTelegramId(long telegramId) {
        return userRepository.findByTelegramId(telegramId).orElseThrow(() -> new EntityNotFoundException("Пользователя с таким id не существует"));
    }

    @Transactional
    public AfterRegisterDataDTO singUp(AuthenticationDTO authenticationDTO){
        if(userRepository.existsByTelegramId(authenticationDTO.getTelegramId())){
            throw new UserAlreadyExistException("Пользователь с таким id уже существует");
        }
        User newUser = convertor.convertToUser(authenticationDTO);
        newUser.setRole(Role.ROLE_USER);

        newUser.setPassword(passwordEncoder.encode(authenticationDTO.getPassword()));

        userRepository.save(newUser);

        String jwt = jwtUtil.generateToken(authenticationDTO.getTelegramId());
        return new AfterRegisterDataDTO(jwt);
    }

    public AfterRegisterDataDTO singIn(AuthenticationDTO authenticationDTO) {
        User user = userRepository.findByTelegramId(authenticationDTO.getTelegramId())
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль!"));

        if (!passwordEncoder.matches(authenticationDTO.getPassword(), user.getPassword())){
            throw new BadCredentialsException("Неверный логин или пароль!");
        }

        String jwt = jwtUtil.generateToken(authenticationDTO.getTelegramId());
        return new AfterRegisterDataDTO(jwt);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long telegramId = Long.valueOf(username);

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new UsernameNotFoundException("User with id not found"));

        return new com.example.greenhouse.security.UserDetails(user);
    }

    @Transactional
    public void updateRoleToAdmin(long id) {
        User user = userRepository.findByTelegramId(id)
                .orElseThrow(()-> new EntityNotFoundException("User with id not found"));

        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    @Transactional
    public void remove(long id) {
        userRepository.deleteById(id);
    }
}
