package com.example.greenhouse.security.token;

import java.util.UUID;

public record DevicePrincipal(UUID deviceId, UUID clusterId) {
}
