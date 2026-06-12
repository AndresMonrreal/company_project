package com.example.company.auth.adapter.out.security;

import com.example.company.auth.domain.port.out.PasswordVerificationPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordVerificationAdapter implements PasswordVerificationPort {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        // Raw passwords are transient credentials; they must never be logged, returned, or stored.
        if (rawPassword == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
