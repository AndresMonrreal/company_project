package com.example.company.auth.domain.port.in;

public record LoginCommand(
        String username,
        String password
) {
}
