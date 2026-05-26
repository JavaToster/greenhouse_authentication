package com.example.greenhouse.security;

import java.util.UUID;

public record DevicePrincipal(UUID deviceId, UUID clusterId) {
}
