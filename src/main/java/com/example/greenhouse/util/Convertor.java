package com.example.greenhouse.util;

import com.example.greenhouse.DTO.auth.AuthenticationDTO;
import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.DTO.cluster.RegisteredClusterDTO;
import com.example.greenhouse.DTO.device.CreatedDeviceDTO;
import com.example.greenhouse.DTO.device.DeviceInfoDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.models.user.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Convertor {
    private final ModelMapper modelMapper;

    public User convertToUser(AuthenticationDTO authenticationDTO){
        return modelMapper.map(authenticationDTO, User.class);
    }

    public List<UserInfoDTO> convertToUserInfoDTO(List<User> all) {
        return all.stream()
                .map(this::convertToUserInfoDTO)
                .toList();
    }

    public UserInfoDTO convertToUserInfoDTO(User user){
        return modelMapper.map(user, UserInfoDTO.class);
    }

    public List<ClusterInfoDTO> convertToClusterInfoDTO(List<Cluster> clusters) {
        return clusters.stream()
                .map(this::convertToClusterInfoDTO)
                .toList();
    }

    public DeviceInfoDTO convertToDeviceInfoDTO(Device device){
        return modelMapper.map(device, DeviceInfoDTO.class);
    }

    public List<DeviceInfoDTO> convertToDeviceInfoDTO(List<Device> devices){
        return devices.stream()
                .map(this::convertToDeviceInfoDTO)
                .toList();
    }

    public ClusterInfoDTO convertToClusterInfoDTO(Cluster cluster){
        ClusterInfoDTO clusterInfoDTO = modelMapper.map(cluster, ClusterInfoDTO.class);

        clusterInfoDTO.setOwner(convertToUserInfoDTO(cluster.getOwner()));
        clusterInfoDTO.setDevices(convertToDeviceInfoDTO(cluster.getDevices()));

        return clusterInfoDTO;
    }
}
