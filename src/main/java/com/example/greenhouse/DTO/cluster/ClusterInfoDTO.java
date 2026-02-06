package com.example.greenhouse.DTO.cluster;

import com.example.greenhouse.DTO.device.DeviceInfoDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ClusterInfoDTO {
    private UUID id;
    private String name;
    private String description;
    private UserInfoDTO owner;
    private List<DeviceInfoDTO> devices;
    private List<UserInfoDTO> workers;
}
