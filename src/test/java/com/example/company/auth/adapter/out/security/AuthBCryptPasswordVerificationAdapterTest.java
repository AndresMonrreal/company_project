package com.example.company.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthBCryptPasswordVerificationAdapterTest {

    private final BCryptPasswordVerificationAdapter adapter = new BCryptPasswordVerificationAdapter();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void verifiesMatchingBCryptHash() {
        String hash = encoder.encode("Secret123!");

        assertThat(adapter.matches("Secret123!", hash)).isTrue();
    }

    @Test
    void rejectsWrongPasswordAgainstBCryptHash() {
        String hash = encoder.encode("Secret123!");

        assertThat(adapter.matches("wrong-password", hash)).isFalse();
    }

    @Test
    void rejectsMissingHashWithoutAttemptingCredentialLeakage() {
        assertThat(adapter.matches("Secret123!", null)).isFalse();
        assertThat(adapter.matches("Secret123!", " ")).isFalse();
        assertThat(adapter.matches(null, "$2a$hash")).isFalse();
    }
}
