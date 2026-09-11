package com.glowup.crm.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final byte[] secret;
    private final long tokenSeconds;

    public TokenService(@Value("${app.auth.secret}") String secret,
                        @Value("${app.auth.token-hours}") long tokenHours) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.tokenSeconds = tokenHours * 3600;
    }

    public String issue(Long adminId) {
        String payload = adminId + "." + (Instant.now().getEpochSecond() + tokenSeconds);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "." + signature(payload);
    }

    public Long verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) return null;
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            if (!constantTimeEquals(signature(payload), parts[1])) return null;
            String[] fields = payload.split("\\.");
            if (fields.length != 2 || Long.parseLong(fields[1]) < Instant.now().getEpochSecond()) return null;
            return Long.parseLong(fields[0]);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String signature(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot sign authentication token", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII), actual.getBytes(StandardCharsets.US_ASCII));
    }
}
