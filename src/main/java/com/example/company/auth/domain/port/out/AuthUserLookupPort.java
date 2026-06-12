package com.example.company.auth.domain.port.out;

import java.util.Optional;

import com.example.company.auth.domain.model.AuthUserRecord;

public interface AuthUserLookupPort {

    Optional<AuthUserRecord> findByUsername(String username);
}
