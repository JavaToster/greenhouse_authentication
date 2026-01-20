package com.example.greenhouse.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeviceTokenUtil {
    private final SecretKeySpec deviceTokenKeySpec;
    private static final char[] SAFE_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final java.math.BigInteger ALPHABET_LEN = java.math.BigInteger.valueOf(SAFE_ALPHABET.length);

    public String generateToken(long telegramId) {
        try{
            long timestamp = Instant.now().getEpochSecond();

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES*2);
            buffer.putLong(telegramId);
            buffer.putLong(timestamp);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(deviceTokenKeySpec);
            byte[] rawHmac = mac.doFinal(buffer.array());

            StringBuilder result = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                int hashPart = ByteBuffer.wrap(rawHmac, i * 2, 4).getInt();
                int index = Math.abs(hashPart) % SAFE_ALPHABET.length;
                result.append(SAFE_ALPHABET[index]);
            }

            return result.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    private String encodeToSafeString(byte[] bytes, int length) {
        StringBuilder result = new StringBuilder();
        java.math.BigInteger number = new java.math.BigInteger(1, bytes);
        java.math.BigInteger alphabetLength = java.math.BigInteger.valueOf(SAFE_ALPHABET.length);

        for (int i = 0; i < length; i++) {
            java.math.BigInteger[] divRem = number.divideAndRemainder(alphabetLength);
            number = divRem[0];
            int remainder = divRem[1].intValue();
            result.append(SAFE_ALPHABET[remainder]);
        }

        return result.toString();
    }
}
