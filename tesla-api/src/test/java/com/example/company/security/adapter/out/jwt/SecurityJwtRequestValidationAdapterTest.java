package com.example.company.security.adapter.out.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.example.company.auth.adapter.out.security.HmacJwtTokenAdapter;
import com.example.company.auth.adapter.out.security.JwtProperties;
import com.example.company.auth.domain.model.AuthenticatedUser;
import com.example.company.security.model.JwtValidationException;
import com.example.company.security.model.AuthenticatedUserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SecurityJwtRequestValidationAdapterTest {

    private static final String SECRET = "test-jwt-secret-value-that-is-not-real";
    private static final Instant NOW = Instant.parse("2026-06-12T18:00:00Z");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void validatesTokenGeneratedByExistingHmacJwtTokenAdapter() {
        JwtProperties properties = jwtProperties(SECRET, "company-test");
        HmacJwtTokenAdapter tokenAdapter = new HmacJwtTokenAdapter(properties);
        JwtRequestValidationAdapter validationAdapter = new JwtRequestValidationAdapter(
                properties,
                objectMapper,
                Clock.systemUTC()
        );

        String token = tokenAdapter.generate(new AuthenticatedUser(1L, "admin", "Initial Administrator", "ADMIN")).token();

        AuthenticatedUserContext context = validationAdapter.validate(token).userContext();

        assertThat(context.userId()).isEqualTo(1L);
        assertThat(context.username()).isEqualTo("admin");
        assertThat(context.role()).isEqualTo("ADMIN");
        assertThat(context.expiresAt()).isAfter(Instant.now().plusSeconds(60));
    }

    @Test
    void rejectsMalformedToken() {
        JwtRequestValidationAdapter adapter = validationAdapter(jwtProperties(SECRET, "company-test"));

        assertInvalidToken(() -> adapter.validate("not-a-jwt"));
    }

    @Test
    void rejectsUnsupportedAlgorithm() {
        JwtRequestValidationAdapter adapter = validationAdapter(jwtProperties(SECRET, "company-test"));
        String token = token(Map.of("alg", "none", "typ", "JWT"), validPayload(), SECRET);

        assertInvalidToken(() -> adapter.validate(token));
    }

    @Test
    void rejectsInvalidSignature() {
        JwtRequestValidationAdapter adapter = validationAdapter(jwtProperties(SECRET, "company-test"));
        String token = token(Map.of("alg", "HS256", "typ", "JWT"), validPayload(), "different-secret");

        assertInvalidToken(() -> adapter.validate(token));
    }

    @Test
    void rejectsExpiredToken() {
        JwtRequestValidationAdapter adapter = validationAdapter(jwtProperties(SECRET, "company-test"));
        Map<String, Object> payload = validPayload();
        payload.put("exp", NOW.minusSeconds(1).getEpochSecond());
        String token = token(Map.of("alg", "HS256", "typ", "JWT"), payload, SECRET);

        assertThatThrownBy(() -> adapter.validate(token))
                .isInstanceOf(JwtValidationException.class)
                .satisfies(ex -> assertThat(((JwtValidationException) ex).reason().code())
                        .isEqualTo("security.expired-token"));
    }

    @Test
    void rejectsInvalidIssuerWhenIssuerIsConfigured() {
        JwtRequestValidationAdapter adapter = validationAdapter(jwtProperties(SECRET, "company-test"));
        Map<String, Object> payload = validPayload();
        payload.put("iss", "wrong-issuer");
        String token = token(Map.of("alg", "HS256", "typ", "JWT"), payload, SECRET);

        assertInvalidToken(() -> adapter.validate(token));
    }

    @Test
    void rejectsMissingRequiredClaims() {
        JwtRequestValidationAdapter adapter = validationAdapter(jwtProperties(SECRET, "company-test"));
        Map<String, Object> payload = validPayload();
        payload.remove("role");
        String token = token(Map.of("alg", "HS256", "typ", "JWT"), payload, SECRET);

        assertInvalidToken(() -> adapter.validate(token));
    }

    private JwtRequestValidationAdapter validationAdapter(JwtProperties properties) {
        return new JwtRequestValidationAdapter(properties, objectMapper, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private JwtProperties jwtProperties(String secret, String issuer) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setExpiration(Duration.ofMinutes(15));
        properties.setIssuer(issuer);
        return properties;
    }

    private Map<String, Object> validPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "1");
        payload.put("userId", 1L);
        payload.put("username", "admin");
        payload.put("role", "ADMIN");
        payload.put("iat", NOW.getEpochSecond());
        payload.put("exp", NOW.plusSeconds(900).getEpochSecond());
        payload.put("iss", "company-test");
        return payload;
    }

    private String token(Map<String, Object> header, Map<String, Object> payload, String secret) {
        try {
            String encodedHeader = encode(header);
            String encodedPayload = encode(payload);
            String unsignedToken = encodedHeader + "." + encodedPayload;
            return unsignedToken + "." + sign(unsignedToken, secret);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not create test token", ex);
        }
    }

    private String encode(Object value) throws Exception {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String unsignedToken, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
    }

    private void assertInvalidToken(ThrowingCallable callable) {
        assertThatThrownBy(callable::call)
                .isInstanceOf(JwtValidationException.class)
                .satisfies(ex -> assertThat(((JwtValidationException) ex).reason().code())
                        .isEqualTo("security.invalid-token"));
    }

    @FunctionalInterface
    private interface ThrowingCallable {
        void call();
    }
}
