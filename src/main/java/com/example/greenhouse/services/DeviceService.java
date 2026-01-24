package com.example.greenhouse.services;

import com.example.greenhouse.DTO.auth.DeviceAuthRequestDTO;
import com.example.greenhouse.DTO.device.CreateDeviceDTO;
import com.example.greenhouse.DTO.device.CreatedDeviceDTO;
import com.example.greenhouse.models.device.Device;
import com.example.greenhouse.models.user.User;
import com.example.greenhouse.repositories.DeviceRepository;
import com.example.greenhouse.repositories.UserRepository;
import com.example.greenhouse.security.JwtUtil;
import com.example.greenhouse.util.enums.DeviceStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final Map<String, String> challengeStorage = new ConcurrentHashMap<>();
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public String generateChallenge(String deviceId) {
        String challenge = UUID.randomUUID().toString();
        challengeStorage.put(deviceId.toString(), challenge);
        return challenge;
    }

    public String verify(DeviceAuthRequestDTO deviceAuthRequestDTO){
        Device device = deviceRepository.findById(deviceAuthRequestDTO.getDeviceId())
                .orElseThrow(() -> new BadCredentialsException("Unknown device"));

        String issuedChallenge = challengeStorage.get(deviceAuthRequestDTO.getDeviceId().toString());
        if(issuedChallenge == null){
            throw new BadCredentialsException("No challenge issued or expired");
        }

        if(!issuedChallenge.equals(deviceAuthRequestDTO.getChallenge())){
            throw new BadCredentialsException("Challenge mismatch");
        }

        validateSignature(deviceAuthRequestDTO.getSignature(), issuedChallenge, device.getSecret());

        return jwtUtil.generateToken(device.getOwner().getTelegramId());
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

    @Transactional
    public CreatedDeviceDTO addNewDevice(CreateDeviceDTO createDeviceDTO) {
        User user = userRepository.findByTelegramId(createDeviceDTO.getTelegramId())
                .orElseThrow(() -> new EntityNotFoundException("User with this id not found!"));

        UUID uuid = UUID.randomUUID();
        String secret = generateSecret();

        Device device = new Device();
        device.setDeviceId(uuid);
        device.setOwner(user);
        device.setSecret(secret);
        device.setStatus(DeviceStatus.ACTIVE);

        deviceRepository.save(device);

        return new CreatedDeviceDTO(uuid.toString(), secret, createDeviceDTO.getTelegramId());
    }

    private String generateSecret(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
