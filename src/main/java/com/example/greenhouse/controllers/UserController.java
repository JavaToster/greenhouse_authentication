package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.user.AssignRoleToPersonDTO;
import com.example.greenhouse.DTO.user.UserInfoBatchRequestDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PatchMapping("/{telegramId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> role(@PathVariable("telegramId") long id, @Valid @RequestBody AssignRoleToPersonDTO assignPersonDTO){
        userService.setRoleOfUser(id, assignPersonDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{telegramId}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> removeUser(@PathVariable("telegramId") long id){
        userService.remove(id);
        return ResponseEntity.ok(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInfoDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/{telegramId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTALLER', 'OWNER')")
    public ResponseEntity<UserInfoDTO> getUser(@PathVariable("telegramId") Long telegramId){
        return ResponseEntity.ok(userService.findUserByTelegramId(telegramId));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<UserInfoDTO>> getUsers(@Valid @RequestBody UserInfoBatchRequestDTO idsDTO){
        return ResponseEntity.ok(userService.findUsersById(idsDTO.getUserIds()));
    }
}
