package com.example.greenhouse.util;

import com.example.greenhouse.DTO.auth.SingUpDTO;
import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.DTO.device.DeviceInfoDTO;
import com.example.greenhouse.DTO.task.TaskInfoDTO;
import com.example.greenhouse.DTO.user.UserInfoDTO;
import com.example.greenhouse.models.Cluster;
import com.example.greenhouse.models.Device;
import com.example.greenhouse.models.Task;
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
        clusterInfoDTO.setWorkers(convertToUserInfoDTO(cluster.getWorkers()));

        return clusterInfoDTO;
    }

    public List<TaskInfoDTO> convertToTaskInfoDTO(List<Task> tasks){
        return tasks.stream()
                .map(this::convertToTaskInfoDTO)
                .toList();
    }

    public TaskInfoDTO convertToTaskInfoDTO(Task task) {
        return modelMapper.map(task, TaskInfoDTO.class);
    }
}
