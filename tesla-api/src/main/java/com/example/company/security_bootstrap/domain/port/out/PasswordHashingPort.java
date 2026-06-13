package com.example.company.security_bootstrap.domain.port.out;

public interface PasswordHashingPort {

    String hash(String rawPassword);
}
