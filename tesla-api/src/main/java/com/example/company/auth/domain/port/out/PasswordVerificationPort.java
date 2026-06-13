package com.example.company.auth.domain.port.out;

public interface PasswordVerificationPort {

    boolean matches(String rawPassword, String passwordHash);
}
