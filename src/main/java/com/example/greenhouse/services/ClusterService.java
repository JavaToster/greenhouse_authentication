package com.example.greenhouse.services;

import com.example.greenhouse.DAO.device.DeviceDAO;
import com.example.greenhouse.DAO.cluster.ClusterDAO;
import com.example.greenhouse.DAO.user.UserDAO;
import com.example.greenhouse.DTO.cluster.ClusterInfoDTO;
import com.example.greenhouse.DTO.cluster.RegisterNewClusterDTO;
import com.example.greenhouse.DTO.device.ClusterDevicesTempSecretsDTO;
import com.example.greenhouse.DTO.device.DevicesSecretWrapper;
import com.example.greenhouse.DTO.device.DevicesTempSecretDTO;
import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.models.user.User;
import com.example.greenhouse.repositories.redis.RedisRepository;
import com.example.greenhouse.util.Convertor;
import com.example.greenhouse.util.enums.DeviceStatus;
import com.example.greenhouse.util.enums.Role;
import com.example.greenhouse.util.redis.RedisKeyCreator;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClusterService {
    private final ClusterDAO clusterDAO;
    private final UserDAO userDAO;
    private final DeviceDAO deviceDAO;
    private final DeviceService deviceService;
    private final Convertor convertor;
    private final RedisRepository redisRepository;
    private final RedisKeyCreator redisKeyCreator;

    private static final int CLUSTER_DEVICES_TEMP_SECRETS_TTL_IN_MINUTES = 5;

    @Transactional
    public DevicesTempSecretDTO registerNewCluster(RegisterNewClusterDTO registerNewClusterDTO) {
        User user = userDAO.find(registerNewClusterDTO.getOwnerId());

        Cluster cluster = new Cluster();
        cluster.setName(registerNewClusterDTO.getName());
        cluster.setOwner(user);

        clusterDAO.save(cluster);

        List<Device> devicesOfCluster = deviceService.createNewDevices(cluster, registerNewClusterDTO.getDevicesCount());
        cluster.setDevices(devicesOfCluster);
        deviceDAO.saveAll(devicesOfCluster);

        List<ClusterDevicesTempSecretsDTO> tempSecrets = devicesOfCluster.stream()
                .map(d -> new ClusterDevicesTempSecretsDTO(d.getId(), d.getRawSecret()))
                .toList();

        UUID secretsToken = UUID.randomUUID();
        redisRepository.saveWithTTLInMinutes(redisKeyCreator.createClusterDevicesTempSecretsKey(secretsToken), new DevicesSecretWrapper(cluster.getId(), tempSecrets), CLUSTER_DEVICES_TEMP_SECRETS_TTL_IN_MINUTES);

        return new DevicesTempSecretDTO(cluster.getId().toString(), secretsToken.toString());
    }

    public List<ClusterInfoDTO> findAllClusters() {
        List<Cluster> clusters = clusterDAO.findAll();

        return convertor.convertToClusterInfoDTO(clusters);
    }

    @Transactional
    public List<ClusterDevicesTempSecretsDTO> getRawKeysAndActivate(UUID token) {
        DevicesSecretWrapper wrapper = redisRepository.findByKey(redisKeyCreator.createClusterDevicesTempSecretsKey(token), DevicesSecretWrapper.class);

        if (wrapper == null || wrapper.getSecrets() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Секреты не найдены или уже были активированы.");
        }

        redisRepository.remove(redisKeyCreator.createClusterDevicesTempSecretsKey(token));

        deviceDAO.updateStatusByClusterId(wrapper.getClusterId(), DeviceStatus.ACTIVE);

        return wrapper.getSecrets();
    }

    public List<ClusterInfoDTO> findByOwnerId(long telegramId) {
        User user = userDAO.find(telegramId);

        List<Cluster> clusters = clusterDAO.find(user);
        return convertor.convertToClusterInfoDTO(clusters);
    }

    @Transactional
    public void addWorkerToCluster(long ownerId, UUID clusterId, long workerId) throws BadRequestException, AccessDeniedException {
        Cluster cluster = clusterDAO.find(clusterId);

        if (cluster.getOwner().getTelegramId() != ownerId){
            throw new AccessDeniedException("Пользователь не является хозяином этого кластера");
        }

        User worker = userDAO.find(workerId);

        if (worker.getRole() == null || worker.getRole() != Role.ROLE_WORKER){
            throw new BadRequestException("Пользователь не является работником!");
        }

        if (!cluster.getWorkers().contains(worker)){
            cluster.addWorker(worker);
        }

        clusterDAO.save(cluster);
    }

    public void removeWorkerFromCluster(long ownerId, UUID clusterId, long workerId) throws AccessDeniedException, BadRequestException {
        Cluster cluster = clusterDAO.find(clusterId);

        if (cluster.getOwner().getTelegramId() != ownerId){
            throw new AccessDeniedException("Пользователь не является хозяином этого кластера");
        }

        User worker = userDAO.find(workerId);

        if (!cluster.getWorkers().contains(worker)){
            throw new BadRequestException("Этот пользователь не привязан к данному кластеру");
        }

        cluster.removeWorker(worker);

        clusterDAO.save(cluster);
    }
}
