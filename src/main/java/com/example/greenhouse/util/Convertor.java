package com.example.greenhouse.util;

import com.example.greenhouse.DTO.auth.SingUpDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.models.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Convertor {
    private final ModelMapper modelMapper;

    public User convertToUser(SingUpDTO authenticationDTO){
        return modelMapper.map(authenticationDTO, User.class);
    }

    public List<UserInfoDTO> convertToUserInfoDTO(Collection<? extends User> all) {
        return all.stream()
                .map(this::convertToUserInfoDTO)
                .toList();
    }

    public UserInfoDTO convertToUserInfoDTO(User user){
        return modelMapper.map(user, UserInfoDTO.class);
    }
}
