package com.example.company.auth.adapter.out.security;

import java.time.Duration;

import com.example.company.auth.domain.exception.TokenConfigurationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties implements InitializingBean {

    // The JWT secret must come from environment/configuration and must never be committed.
    private String secret;
    private Duration expiration = Duration.ofMinutes(15);
    private String issuer;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String requiredSecret() {
        if (secret == null || secret.isBlank()) {
            throw new TokenConfigurationException("JWT signing secret is required");
        }
        return secret;
    }

    public Duration requiredExpiration() {
        if (expiration == null || expiration.isZero() || expiration.isNegative()) {
            throw new TokenConfigurationException("JWT expiration must be positive");
        }
        return expiration;
    }

    public boolean hasIssuer() {
        return issuer != null && !issuer.isBlank();
    }

    public String normalizedIssuer() {
        return hasIssuer() ? issuer.trim() : null;
    }

    @Override
    public void afterPropertiesSet() {
        requiredSecret();
        requiredExpiration();
    }
}
