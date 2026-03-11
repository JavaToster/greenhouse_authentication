package com.example.greenhouse.services;

import com.example.greenhouse.DTO.auth.SingInDTO;
import com.example.greenhouse.store.UserStore;
import com.example.greenhouse.DTO.admin.AssignRoleToPersonDTO;
import com.example.greenhouse.DTO.auth.AfterRegisterDataDTO;
import com.example.greenhouse.DTO.auth.SingUpDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.exceptions.auth.UserAlreadyExistException;
import com.example.greenhouse.models.User;
import com.example.greenhouse.security.CustomUserDetailsService;
import com.example.greenhouse.security.token.UserTokenService;
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
    private final UserStore userDAO;
    private final UserTokenService userTokenService;
    private final Convertor convertor;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findUserByTelegramId(long telegramId) {
        return userDAO.findById(telegramId);
    }

    @Transactional
    public AfterRegisterDataDTO singUp(SingUpDTO authenticationDTO){
        log.info("Attempting signup for user {}", authenticationDTO.getTelegramId());
        if(userDAO.exist(authenticationDTO.getTelegramId(), authenticationDTO.getEmail())){
            throw new UserAlreadyExistException("User already exists");
        }
        User newUser = convertor.convertToUser(authenticationDTO);
        newUser.setRole(Role.ROLE_UNKNOWN);
        newUser.setPassword(passwordEncoder.encode(authenticationDTO.getPassword()));
        userDAO.save(newUser);
        log.info("User {} registered successfully", newUser.getTelegramId());

        return new AfterRegisterDataDTO(userTokenService.generate(authenticationDTO.getTelegramId()));
    }

    public AfterRegisterDataDTO singIn(SingInDTO authenticationDTO) {
        log.info("Sign-in attempt for user {}", authenticationDTO.getTelegramId());
        User user = userDAO.findById(authenticationDTO.getTelegramId());

        if (!passwordEncoder.matches(authenticationDTO.getPassword(), user.getPassword())){
            throw new BadCredentialsException("Invalid login or password");
        }

        log.info("User {} signed in successfully", user.getTelegramId());
        return new AfterRegisterDataDTO(userTokenService.generate(authenticationDTO.getTelegramId()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDAO.findById(Long.parseLong(username));
        return new com.example.greenhouse.security.UserDetails(user);
    }

    @Transactional
    public void setRoleOfUser(long id, AssignRoleToPersonDTO assignRoleToPersonDTO) {
        log.info("Updating role for user {} to {}", id, assignRoleToPersonDTO.getRole());
        User user = userDAO.findById(id);
        user.setRole(Role.valueOf(assignRoleToPersonDTO.getRole()));
        userDAO.save(user);
    }

    @Transactional
    public void remove(long id) {
        log.info("Removing user {}", id);
        userDAO.remove(id);
    }

    public List<UserInfoDTO> findAllUsers() {
        return convertor.convertToUserInfoDTO(userDAO.findAll());
    }
}
