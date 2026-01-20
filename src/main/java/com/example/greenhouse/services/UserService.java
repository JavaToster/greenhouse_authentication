package com.example.greenhouse.services;

import com.example.greenhouse.DTO.auth.AfterRegisterDataDTO;
import com.example.greenhouse.DTO.auth.AuthenticationDTO;
import com.example.greenhouse.exceptions.auth.UserAlreadyExistException;
import com.example.greenhouse.models.user.User;
import com.example.greenhouse.repositories.UserRepository;
import com.example.greenhouse.security.CustomUserDetailsService;
import com.example.greenhouse.security.DeviceTokenUtil;
import com.example.greenhouse.security.JwtUtil;
import com.example.greenhouse.util.Convertor;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final DeviceTokenUtil deviceTokenUtil;
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

        String token = deviceTokenUtil.generateToken(authenticationDTO.getTelegramId());
        String jwt = jwtUtil.generateToken(authenticationDTO.getTelegramId());

        newUser.setDeviceToken(token);
        newUser.setPassword(passwordEncoder.encode(authenticationDTO.getPassword()));

        userRepository.save(newUser);

        return new AfterRegisterDataDTO(jwt, token);
    }

    public AfterRegisterDataDTO singIn(AuthenticationDTO authenticationDTO) {
        User user = userRepository.findByTelegramId(authenticationDTO.getTelegramId())
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль!"));

        if (!passwordEncoder.matches(authenticationDTO.getPassword(), user.getPassword())){
            throw new BadCredentialsException("Неверный логин или пароль!");
        }

        String jwt = jwtUtil.generateToken(authenticationDTO.getTelegramId());
        return new AfterRegisterDataDTO(jwt, user.getDeviceToken());
    }
}
