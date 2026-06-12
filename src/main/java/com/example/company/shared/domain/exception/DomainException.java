package com.example.company.shared.domain.exception;

public abstract class DomainException extends RuntimeException {

    private final DomainErrorType type;
    private final String code;

    protected DomainException(DomainErrorType type, String code, String message) {
        super(message);
        this.type = type;
        this.code = code;
    }

    public DomainErrorType type() {
        return type;
    }

    public String code() {
        return code;
    }
}
