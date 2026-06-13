package com.example.company.security.model;

public class JwtValidationException extends RuntimeException {

    private final Reason reason;

    public JwtValidationException(Reason reason) {
        super(reason.safeMessage());
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        INVALID_TOKEN("security.invalid-token", "Invalid authentication token"),
        EXPIRED_TOKEN("security.expired-token", "Authentication token has expired");

        private final String code;
        private final String safeMessage;

        Reason(String code, String safeMessage) {
            this.code = code;
            this.safeMessage = safeMessage;
        }

        public String code() {
            return code;
        }

        public String safeMessage() {
            return safeMessage;
        }
    }
}
