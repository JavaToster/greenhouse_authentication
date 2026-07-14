package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.user.AssignRoleToPersonDTO;
import com.example.greenhouse.DTO.user.UserInfoBatchRequestDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Administrative endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @PatchMapping("/{telegramId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role updated")
    })
    public ResponseEntity<Long> role(@PathVariable("telegramId") long id, @Valid @RequestBody AssignRoleToPersonDTO assignPersonDTO) {
        log.info("Received request to update role for user id={} to role={}", id, assignPersonDTO.role());
        userService.setRoleOfUser(id, assignPersonDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{telegramId}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted")
    })
    public ResponseEntity<Long> removeUser(@PathVariable("telegramId") long id) {
        log.info("Received request to remove user id={}", id);
        userService.remove(id);
        return ResponseEntity.ok(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned successfully")
    })
    public ResponseEntity<List<UserInfoDTO>> getAllUsers() {
        log.debug("Received request to fetch all users");
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/{telegramId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTALLER', 'OWNER')")
    @Operation(summary = "Get user by Telegram ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User returned successfully")
    })
    public ResponseEntity<UserInfoDTO> getUser(@PathVariable("telegramId") Long telegramId) {
        log.debug("Received request to fetch user by telegramId={}", telegramId);
        return ResponseEntity.ok(userService.findUserByTelegramId(telegramId));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'INSTALLER')")
    @Operation(summary = "Batch fetch users by Telegram IDs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned successfully")
    })
    public ResponseEntity<List<UserInfoDTO>> getUsers(@Valid @RequestBody UserInfoBatchRequestDTO idsDTO) {
        log.debug("Received batch request to fetch {} users", idsDTO.userIds().size());
        return ResponseEntity.ok(userService.findUsersById(idsDTO.userIds()));
    }
}