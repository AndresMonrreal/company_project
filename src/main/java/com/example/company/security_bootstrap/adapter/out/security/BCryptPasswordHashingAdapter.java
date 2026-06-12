package com.example.company.security_bootstrap.adapter.out.security;

import com.example.company.security_bootstrap.domain.port.out.PasswordHashingPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHashingAdapter implements PasswordHashingPort {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Raw password is required");
        }

        return passwordEncoder.encode(rawPassword);
    }
}
