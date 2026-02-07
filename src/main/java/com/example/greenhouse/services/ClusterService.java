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
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Slf4j
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
        log.info("Starting new cluster registration with name '{}' for owner {}", registerNewClusterDTO.getName(), registerNewClusterDTO.getOwnerId());
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

        log.info("Cluster {} with name {} successfully registered. Device count: {}. Owner: {}", cluster.getId(), cluster.getName(), cluster.getDevices().size(), cluster.getOwner().getTelegramId());

        return new DevicesTempSecretDTO(cluster.getId().toString(), secretsToken.toString());
    }

    public List<ClusterInfoDTO> findAllClusters() {
        List<Cluster> clusters = clusterDAO.findAll();

        return convertor.convertToClusterInfoDTO(clusters);
    }

    @Transactional
    public List<ClusterDevicesTempSecretsDTO> getRawKeysAndActivate(UUID token) {
        log.info("Attempting to activate devices using token {}", token);
        DevicesSecretWrapper wrapper = redisRepository.findByKey(redisKeyCreator.createClusterDevicesTempSecretsKey(token), DevicesSecretWrapper.class);

        if (wrapper == null || wrapper.getSecrets() == null) {
            log.warn("Device activation error: token {} does not exist or has expired", token);
            throw new org.springframework.security.access.AccessDeniedException("Secrets not found or already activated.");
        }

        redisRepository.remove(redisKeyCreator.createClusterDevicesTempSecretsKey(token));

        deviceDAO.updateStatusByClusterId(wrapper.getClusterId(), DeviceStatus.ACTIVE);

        log.info("Devices for cluster {} successfully activated", wrapper.getClusterId());

        return wrapper.getSecrets();
    }

    public List<ClusterInfoDTO> findByOwnerId(long telegramId) {
        User user = userDAO.find(telegramId);

        List<Cluster> clusters = clusterDAO.findByOwner(user);
        return convertor.convertToClusterInfoDTO(clusters);
    }

    @Transactional
    public void addWorkerToCluster(long ownerId, UUID clusterId, long workerId) throws BadRequestException, AccessDeniedException {
        log.info("Attempting to add worker {} to cluster {}", workerId, clusterId);

        Cluster cluster = clusterDAO.findById(clusterId);

        checkOwner(cluster, ownerId);

        User worker = userDAO.find(workerId);

        isWorker(worker);

        if (cluster.getWorkers().contains(worker)) {
            log.warn("User {} is already a worker in cluster {}", workerId, clusterId);
            throw new BadRequestException("User is already a worker in this cluster");
        }

        cluster.addWorker(worker);
        clusterDAO.save(cluster);

        log.info("Worker {} successfully added to cluster {}", workerId, clusterId);
    }

    public void removeWorkerFromCluster(long ownerId, UUID clusterId, long workerId) throws AccessDeniedException, BadRequestException {
        log.info("Attempting to remove worker {} from cluster {} by owner {}", workerId, clusterId, ownerId);
        Cluster cluster = clusterDAO.findById(clusterId);

        checkOwner(cluster, ownerId);

        User worker = userDAO.find(workerId);

        isWorker(worker);

        if (!cluster.getWorkers().contains(worker)) {
            log.warn("User {} is not a worker in cluster {}", workerId, clusterId);
            throw new BadRequestException("User is not a worker in this cluster");
        }
        cluster.removeWorker(worker);
        clusterDAO.save(cluster);
        log.info("Worker {} successfully removed from cluster {}", workerId, clusterId);
    }

    public List<ClusterInfoDTO> findByWorker(long workerId) {
        return convertor.convertToClusterInfoDTO(clusterDAO.findByWorker(workerId));
    }

    private void checkOwner(Cluster cluster, long ownerId){
        if(cluster.getOwner().getTelegramId() != ownerId){
            log.warn("Security alert: User {} is not the owner of cluster {}", ownerId, cluster.getId());
            throw new AccessDeniedException("User is not the owner of this cluster");
        }
    }

    private void isWorker(User worker) throws BadRequestException {
        if (worker.getRole() != Role.ROLE_WORKER){
            log.warn("User {} does not have the WORKER role", worker.getTelegramId());
            throw new BadRequestException("User is not a worker");
        }
    }
}