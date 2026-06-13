package com.example.company.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AuthLoginCredentialsTest {

    @Test
    void trimsUsernameAndKeepsPasswordAvailableOnlyAsCredentialValue() {
        LoginCredentials credentials = new LoginCredentials(" admin ", "Secret123!");

        assertThat(credentials.username()).isEqualTo("admin");
        assertThat(credentials.password()).isEqualTo("Secret123!");
    }

    @Test
    void rejectsBlankUsername() {
        assertThatThrownBy(() -> new LoginCredentials(" ", "Secret123!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is required");
    }

    @Test
    void rejectsBlankPassword() {
        assertThatThrownBy(() -> new LoginCredentials("admin", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");
    }

    @Test
    void redactsRawPasswordFromToString() {
        LoginCredentials credentials = new LoginCredentials("admin", "Secret123!");

        assertThat(credentials.toString())
                .contains("admin")
                .contains("password=<redacted>")
                .doesNotContain("Secret123!");
    }
}
