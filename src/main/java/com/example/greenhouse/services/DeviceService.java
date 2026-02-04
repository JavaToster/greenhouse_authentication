package com.example.greenhouse.services;

import com.example.greenhouse.DTO.auth.DeviceAuthRequestDTO;
import com.example.greenhouse.DTO.device.CreateDeviceDTO;
import com.example.greenhouse.DTO.device.CreatedDeviceDTO;
import com.example.greenhouse.models.clusters.Cluster;
import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.models.user.User;
import com.example.greenhouse.repositories.postgres.DeviceRepository;
import com.example.greenhouse.repositories.postgres.UserRepository;
import com.example.greenhouse.repositories.redis.RedisRepository;
import com.example.greenhouse.security.EncryptionUtil;
import com.example.greenhouse.security.JwtUtil;
import com.example.greenhouse.util.enums.DeviceStatus;
import com.example.greenhouse.util.redis.RedisKeyCreator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final JwtUtil jwtUtil;
    private static final long CHALLENGE_TTL_IN_SECONDS = 30;
    private final RedisRepository redisRepository;
    private final RedisKeyCreator redisKeyCreator;
    private final EncryptionUtil encryptionUtil;

    public String generateChallenge(String deviceId) {
        String challenge = UUID.randomUUID().toString();

        redisRepository.saveWithTTLInSeconds(redisKeyCreator.createChallengeKey(deviceId), challenge, CHALLENGE_TTL_IN_SECONDS);

        return challenge;
    }

    public String verify(DeviceAuthRequestDTO deviceAuthRequestDTO){
        Device device = deviceRepository.findById(deviceAuthRequestDTO.getDeviceId())
                .orElseThrow(() -> new BadCredentialsException("Unknown device"));

        String issuedChallenge = redisRepository.findByKey(redisKeyCreator.createChallengeKey(deviceAuthRequestDTO.getDeviceId().toString()), String.class);

        if(issuedChallenge == null){
            throw new BadCredentialsException("No challenge issued or expired");
        }

        if(!issuedChallenge.equals(deviceAuthRequestDTO.getChallenge())){
            throw new BadCredentialsException("Challenge mismatch");
        }

        validateSignature(deviceAuthRequestDTO.getSignature(), issuedChallenge, device.getSecret());

        redisRepository.remove(redisKeyCreator.createChallengeKey(deviceAuthRequestDTO.getDeviceId().toString()));

        return jwtUtil.generateToken(device.getCluster().getOwner().getTelegramId());
    }

    private void validateSignature(String clientSig, String data, String secret){
        String expected = hmacSha256(data, secret);

        if(!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                clientSig.getBytes(StandardCharsets.UTF_8))){
            throw new BadCredentialsException("Invalid signature");
        }
    }

    private String hmacSha256(String data, String secret){
        try{
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            mac.init(key);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Device> createNewDevices(Cluster cluster, int count){
        return IntStream.range(0, count)
                .mapToObj(i -> createNewDevice(cluster))
                .toList();
    }

    public Device createNewDevice(Cluster cluster) {
        Device device = new Device();

        device.setId(UUID.randomUUID());
        device.setCluster(cluster);
        device.setStatus(DeviceStatus.PENDING_ACTIVAT);

        String rawSecret = generateSecret();
        device.setSecret(encryptionUtil.encrypt(rawSecret));
        device.setRawSecret(rawSecret);

        return device;
    }

    private String generateSecret(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Transactional
    public UUID remove(UUID deviceId) {
        deviceRepository.deleteById(deviceId);
        return deviceId;
    }
}
