package com.example.greenhouse.controllers;

import com.example.greenhouse.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PatchMapping("/{telegramId}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> promoteToAdmin(@PathVariable("telegramId") long id){
        userService.updateRoleToAdmin(id);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{telegramId}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeUser(@PathVariable("telegramId") long id){
        userService.remove(id);
        return ResponseEntity.ok(id);
    }
}
