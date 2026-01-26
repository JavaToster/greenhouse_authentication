package com.example.greenhouse.util;

import com.example.greenhouse.DTO.auth.AuthenticationDTO;
import com.example.greenhouse.models.user.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Convertor {
    private final ModelMapper modelMapper;

    public User convertToUser(AuthenticationDTO authenticationDTO){
        return modelMapper.map(authenticationDTO, User.class);
    }
}
