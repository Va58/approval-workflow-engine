package com.project.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtUtil {

    private static final String SECRET = "TECH_PULSE_APPROVAL_ENGINE_2026";

    public String generateToken(String email, String role) {
        long timestamp = System.currentTimeMillis();
        String data = email + ":" + role + ":" + timestamp + ":" + SECRET;
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            return parts.length == 4 && SECRET.equals(parts[3]);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        return decoded.split(":")[0];
    }

    public String extractRole(String token) {
        String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        return decoded.split(":")[1];
    }
}
