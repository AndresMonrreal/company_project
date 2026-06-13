package com.example.company.security.adapter.out.jwt;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.example.company.auth.adapter.out.security.JwtProperties;
import com.example.company.security.model.AuthenticatedUserContext;
import com.example.company.security.model.JwtValidationException;
import com.example.company.security.model.JwtValidationResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtRequestValidationAdapter {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "SUPERVISOR", "OPERADOR", "CONSULTA");

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public JwtRequestValidationAdapter(JwtProperties properties) {
        this(properties, new ObjectMapper(), Clock.systemUTC());
    }

    JwtRequestValidationAdapter(JwtProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public JwtValidationResult validate(String token) {
        if (token == null || token.isBlank()) {
            throw invalid();
        }

        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw invalid();
        }

        Map<String, Object> header = decodeJson(parts[0]);
        Map<String, Object> payload = decodeJson(parts[1]);

        validateHeader(header);
        validateSignature(parts[0] + "." + parts[1], parts[2]);

        Long userId = requiredLong(payload, "userId");
        String subject = requiredText(payload, "sub");
        if (!subject.equals(userId.toString())) {
            throw invalid();
        }

        String username = requiredText(payload, "username");
        String role = requiredText(payload, "role");
        if (!ALLOWED_ROLES.contains(role)) {
            throw invalid();
        }

        requiredLong(payload, "iat");
        long expiresAtEpoch = requiredLong(payload, "exp");
        Instant expiresAt = Instant.ofEpochSecond(expiresAtEpoch);
        if (!expiresAt.isAfter(Instant.now(clock))) {
            throw new JwtValidationException(JwtValidationException.Reason.EXPIRED_TOKEN);
        }

        if (properties.hasIssuer()) {
            String issuer = requiredText(payload, "iss");
            if (!properties.normalizedIssuer().equals(issuer)) {
                throw invalid();
            }
        }

        return new JwtValidationResult(new AuthenticatedUserContext(userId, username, role, expiresAt));
    }

    private void validateHeader(Map<String, Object> header) {
        if (!"HS256".equals(header.get("alg"))) {
            throw invalid();
        }

        Object type = header.get("typ");
        if (type != null && !"JWT".equals(type)) {
            throw invalid();
        }
    }

    private void validateSignature(String unsignedToken, String providedSignature) {
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8)
        )) {
            throw invalid();
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(properties.requiredSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw invalid();
        }
    }

    private Map<String, Object> decodeJson(String encoded) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encoded);
            return objectMapper.readValue(bytes, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw invalid();
        }
    }

    private String requiredText(Map<String, Object> payload, String claimName) {
        Object value = payload.get(claimName);
        if (!(value instanceof String text) || text.isBlank()) {
            throw invalid();
        }
        return text.trim();
    }

    private Long requiredLong(Map<String, Object> payload, String claimName) {
        Object value = payload.get(claimName);
        if (!(value instanceof Number number)) {
            throw invalid();
        }
        return number.longValue();
    }

    private JwtValidationException invalid() {
        return new JwtValidationException(JwtValidationException.Reason.INVALID_TOKEN);
    }
}
