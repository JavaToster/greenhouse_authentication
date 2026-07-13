package com.example.greenhouse.services;

import com.example.greenhouse.DTO.auth.SingInDTO;
import com.example.greenhouse.security.jwt.JwtAuthenticationProvider;
import com.example.greenhouse.store.UserStore;
import com.example.greenhouse.DTO.user.AssignRoleToPersonDTO;
import com.example.greenhouse.DTO.auth.SuccessfullyAuthenticatedDTO;
import com.example.greenhouse.DTO.auth.SingUpDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.exceptions.auth.UserAlreadyExistException;
import com.example.greenhouse.models.User;
import com.example.greenhouse.util.Convertor;
import com.example.greenhouse.util.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements CustomUserDetailsService {
    private final UserStore userStore;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final Convertor convertor;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoDTO findUserByTelegramId(long telegramId) {
        return convertor.convertToUserInfoDTO(userStore.findById(telegramId));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SuccessfullyAuthenticatedDTO singUp(SingUpDTO authenticationDTO){
        log.info("Attempting signup for user {}", authenticationDTO.telegramId());
        if(userStore.exist(authenticationDTO.telegramId(), authenticationDTO.email())){
            throw new UserAlreadyExistException("User already exists");
        }
        User newUser = convertor.convertToUser(authenticationDTO);
        newUser.setRole(Role.ROLE_UNKNOWN);
        newUser.setPassword(passwordEncoder.encode(authenticationDTO.password()));
        userStore.save(newUser);
        log.info("User {} registered successfully", newUser.getTelegramId());

        return new SuccessfullyAuthenticatedDTO(jwtAuthenticationProvider.generate(authenticationDTO.telegramId(), Role.ROLE_UNKNOWN));
    }

    public SuccessfullyAuthenticatedDTO singIn(SingInDTO authenticationDTO) {
        log.info("Sign-in attempt for user {}", authenticationDTO.telegramId());
        User user = userStore.findById(authenticationDTO.telegramId());

        if (!passwordEncoder.matches(authenticationDTO.password(), user.getPassword())){
            throw new BadCredentialsException("Invalid login or password");
        }

        log.info("User {} signed in successfully", user.getTelegramId());
        return new SuccessfullyAuthenticatedDTO(jwtAuthenticationProvider.generate(authenticationDTO.telegramId(), user.getRole()));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void setRoleOfUser(long id, AssignRoleToPersonDTO assignRoleToPersonDTO) {
        log.info("Updating role for user {} to {}", id, assignRoleToPersonDTO.role());
        User user = userStore.findById(id);
        user.setRole(Role.valueOf(assignRoleToPersonDTO.role()));
        userStore.save(user);
    }

    @Transactional
    public void remove(long id) {
        log.info("Removing user {}", id);
        userStore.remove(id);
    }

    public List<UserInfoDTO> findAllUsers() {
        return convertor.convertToUserInfoDTO(userStore.findAll());
    }

    public List<UserInfoDTO> findUsersById(Set<Long> ids){
        List<User> users = userStore.findByIds(ids);
        return convertor.convertToUserInfoDTO(users);
    }
}
