package com.example.company.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;

import com.example.company.auth.domain.exception.TokenConfigurationException;
import com.example.company.auth.domain.model.AuthenticatedUser;
import com.example.company.auth.domain.model.JwtAccessToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AuthHmacJwtTokenAdapterTest {

    private static final Instant NOW = Instant.parse("2026-06-12T18:00:00Z");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void generatesHmacJwtWithRequiredClaimsAndConfiguredIssuer() throws Exception {
        HmacJwtTokenAdapter adapter = new HmacJwtTokenAdapter(
                jwtProperties("test-jwt-secret-value-that-is-not-real", Duration.ofMinutes(15), "company-test"),
                objectMapper,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        JwtAccessToken token = adapter.generate(new AuthenticatedUser(1L, "admin", "Initial Administrator", "ADMIN"));

        String[] parts = token.token().split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(token.expiresAt()).isEqualTo(Instant.parse("2026-06-12T18:15:00Z"));

        Map<String, Object> header = decode(parts[0]);
        Map<String, Object> payload = decode(parts[1]);

        assertThat(header).containsEntry("alg", "HS256").containsEntry("typ", "JWT");
        assertThat(payload)
                .containsEntry("sub", "1")
                .containsEntry("userId", 1)
                .containsEntry("username", "admin")
                .containsEntry("role", "ADMIN")
                .containsEntry("iat", 1781287200)
                .containsEntry("exp", 1781288100)
                .containsEntry("iss", "company-test");
        assertThat(payload).doesNotContainKeys("password", "password_hash", "passwordHash");
    }

    @Test
    void omitsIssuerClaimWhenIssuerIsNotConfigured() throws Exception {
        HmacJwtTokenAdapter adapter = new HmacJwtTokenAdapter(
                jwtProperties("test-jwt-secret-value-that-is-not-real", Duration.ofMinutes(15), " "),
                objectMapper,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        JwtAccessToken token = adapter.generate(new AuthenticatedUser(1L, "admin", "Initial Administrator", "ADMIN"));

        assertThat(decode(token.token().split("\\.")[1])).doesNotContainKey("iss");
    }

    @Test
    void rejectsMissingSigningSecret() {
        HmacJwtTokenAdapter adapter = new HmacJwtTokenAdapter(
                jwtProperties(" ", Duration.ofMinutes(15), null),
                objectMapper,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> adapter.generate(new AuthenticatedUser(1L, "admin", "Initial Administrator", "ADMIN")))
                .isInstanceOf(TokenConfigurationException.class)
                .hasMessage("JWT signing secret is required")
                .satisfies(ex -> assertThat(((TokenConfigurationException) ex).code())
                        .isEqualTo("auth.token-config-invalid"));
    }

    private JwtProperties jwtProperties(String secret, Duration expiration, String issuer) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setExpiration(expiration);
        properties.setIssuer(issuer);
        return properties;
    }

    private Map<String, Object> decode(String encoded) throws Exception {
        byte[] bytes = Base64.getUrlDecoder().decode(encoded);
        return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8), new TypeReference<>() {
        });
    }
}
