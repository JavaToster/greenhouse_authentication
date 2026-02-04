package com.example.greenhouse.services;

import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.DTO.cluster.RegisterNewClusterDTO;
import com.example.greenhouse.DTO.cluster.RegisteredClusterDTO;
import com.example.greenhouse.DTO.device.ClusterDevicesTempSecretsDTO;
import com.example.greenhouse.DTO.device.CreatedDeviceDTO;
import com.example.greenhouse.DTO.device.DevicesSecretWrapper;
import com.example.greenhouse.DTO.device.DevicesTempSecretDTO;
import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.models.user.User;
import com.example.greenhouse.repositories.postgres.ClusterRepository;
import com.example.greenhouse.repositories.postgres.DeviceRepository;
import com.example.greenhouse.repositories.postgres.UserRepository;
import com.example.greenhouse.repositories.redis.RedisRepository;
import com.example.greenhouse.util.Convertor;
import com.example.greenhouse.util.enums.DeviceStatus;
import com.example.greenhouse.util.redis.RedisKeyCreator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClusterService {
    private final ClusterRepository clusterRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final Convertor convertor;
    private final RedisRepository redisRepository;
    private final RedisKeyCreator redisKeyCreator;

    private static final int CLUSTER_DEVICES_TEMP_SECRETS_TTL_IN_MINUTES = 5;

    @Transactional
    public DevicesTempSecretDTO registerNewCluster(RegisterNewClusterDTO registerNewClusterDTO) {
        User user = userRepository.findByTelegramId(registerNewClusterDTO.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователя с таким айди не существует"));

        Cluster cluster = new Cluster();
        cluster.setName(registerNewClusterDTO.getName());
        cluster.setOwner(user);

        clusterRepository.save(cluster);

        List<Device> devicesOfCluster = deviceService.createNewDevices(cluster, registerNewClusterDTO.getDevicesCount());
        cluster.setDevices(devicesOfCluster);
        deviceRepository.saveAll(devicesOfCluster);

        List<ClusterDevicesTempSecretsDTO> tempSecrets = devicesOfCluster.stream()
                .map(d -> new ClusterDevicesTempSecretsDTO(d.getId(), d.getRawSecret()))
                .toList();

        UUID secretsToken = UUID.randomUUID();
        redisRepository.saveWithTTLInMinutes(redisKeyCreator.createClusterDevicesTempSecretsKey(secretsToken), new DevicesSecretWrapper(cluster.getId(), tempSecrets), CLUSTER_DEVICES_TEMP_SECRETS_TTL_IN_MINUTES);

        return new DevicesTempSecretDTO(cluster.getId().toString(), secretsToken.toString());
    }

    public List<ClusterInfoDTO> findAllClusters() {
        List<Cluster> clusters = clusterRepository.findAll();

        return convertor.convertToClusterInfoDTO(clusters);
    }

    @Transactional
    public List<ClusterDevicesTempSecretsDTO> getRawKeysAndActivate(UUID token) {
        DevicesSecretWrapper wrapper = redisRepository.findByKey(redisKeyCreator.createClusterDevicesTempSecretsKey(token), DevicesSecretWrapper.class);

        if (wrapper == null || wrapper.getSecrets() == null) {
            throw new EntityNotFoundException("Секреты не найдены или уже были активированы.");
        }

        redisRepository.remove(redisKeyCreator.createClusterDevicesTempSecretsKey(token));

        deviceRepository.updateStatusByClusterId(wrapper.getClusterId(), DeviceStatus.ACTIVE);

        return wrapper.getSecrets();
    }
}
