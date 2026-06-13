package com.example.company.auth.domain.exception;

public class TokenConfigurationException extends RuntimeException {

    public TokenConfigurationException(String message) {
        super(message);
    }

    public String code() {
        return "auth.token-config-invalid";
    }
}
