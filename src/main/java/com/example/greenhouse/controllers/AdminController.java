package com.example.greenhouse.controllers;

import com.example.greenhouse.DTO.admin.AssignRoleToPersonDTO;
import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.services.ClusterService;
import com.example.greenhouse.services.DeviceService;
import com.example.greenhouse.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final DeviceService deviceService;
    private final ClusterService clusterService;

    @PatchMapping("/users/{telegramId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> role(@PathVariable("telegramId") long id, @Valid @RequestBody AssignRoleToPersonDTO assignPersonDTO){
        userService.setRoleOfUser(id, assignPersonDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/users/{telegramId}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> removeUser(@PathVariable("telegramId") long id){
        userService.remove(id);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/clusters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClusterInfoDTO>> getAllClusters(){
        List<ClusterInfoDTO> clusters = clusterService.findAllClusters();
        return ResponseEntity.ok(clusters);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInfoDTO>> getAllUsers(){
        List<UserInfoDTO> userInfoDTOList = userService.findAllUsers();
        return ResponseEntity.ok(userInfoDTOList);
    }

    @DeleteMapping("/devices/{id}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UUID> removeDevice(@PathVariable("id") UUID deviceId){
        return ResponseEntity.ok(deviceService.remove(deviceId));
    }
}
