package com.example.company.auth.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.example.company.auth.domain.exception.TokenConfigurationException;
import com.example.company.auth.domain.model.AuthenticatedUser;
import com.example.company.auth.domain.model.JwtAccessToken;
import com.example.company.auth.domain.port.out.JwtTokenPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HmacJwtTokenAdapter implements JwtTokenPort {

    private static final String HMAC_SHA_256 = "HmacSHA256";

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public HmacJwtTokenAdapter(JwtProperties properties) {
        this(properties, new ObjectMapper(), Clock.systemUTC());
    }

    HmacJwtTokenAdapter(JwtProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public JwtAccessToken generate(AuthenticatedUser user) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(properties.requiredExpiration());

        String header = encodeJson(Map.of(
                "alg", "HS256",
                "typ", "JWT"
        ));
        String payload = encodeJson(payload(user, issuedAt, expiresAt));
        String unsignedToken = header + "." + payload;

        return new JwtAccessToken(unsignedToken + "." + sign(unsignedToken), expiresAt);
    }

    private Map<String, Object> payload(AuthenticatedUser user, Instant issuedAt, Instant expiresAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.userId().toString());
        payload.put("userId", user.userId());
        payload.put("username", user.username());
        payload.put("role", user.role());
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());
        if (properties.hasIssuer()) {
            payload.put("iss", properties.normalizedIssuer());
        }

        // JWTs carry only safe identity and role claims; password hashes and sensitive data must never enter tokens.
        return payload;
    }

    private String encodeJson(Object value) {
        try {
            return base64Url(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException ex) {
            throw new TokenConfigurationException("JWT payload could not be serialized");
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(properties.requiredSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return base64Url(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new TokenConfigurationException("JWT token could not be signed");
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
