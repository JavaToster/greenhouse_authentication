package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.admin.AssignRoleToPersonDTO;
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
public class UserController {
    private final UserService userService;

    @PatchMapping("/api/admin/users/{telegramId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> role(@PathVariable("telegramId") long id, @Valid @RequestBody AssignRoleToPersonDTO assignPersonDTO){
        userService.setRoleOfUser(id, assignPersonDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/api/admin/users/{telegramId}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> removeUser(@PathVariable("telegramId") long id){
        userService.remove(id);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInfoDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.findAllUsers());
    }
}
