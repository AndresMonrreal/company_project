package com.example.company.auth.domain.exception;

public class InactiveUserException extends RuntimeException {

    public InactiveUserException() {
        super("User is inactive");
    }

    public String code() {
        return "auth.inactive-user";
    }
}
