package com.example.company.auth.domain.exception;

public class RoleUnavailableException extends RuntimeException {

    public RoleUnavailableException() {
        super("User role is unavailable");
    }

    public String code() {
        return "auth.role-unavailable";
    }
}
