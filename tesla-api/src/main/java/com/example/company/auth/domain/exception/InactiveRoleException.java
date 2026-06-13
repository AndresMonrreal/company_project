package com.example.company.auth.domain.exception;

public class InactiveRoleException extends RuntimeException {

    public InactiveRoleException() {
        super("User role is inactive");
    }

    public String code() {
        return "auth.inactive-role";
    }
}
