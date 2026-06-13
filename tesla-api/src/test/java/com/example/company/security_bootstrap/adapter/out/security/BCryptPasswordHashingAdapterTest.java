package com.example.company.security_bootstrap.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptPasswordHashingAdapterTest {

    private final BCryptPasswordHashingAdapter adapter = new BCryptPasswordHashingAdapter();

    @Test
    void hashesPasswordWithBCrypt() {
        String hash = adapter.hash("admin-secret");

        assertThat(hash).isNotEqualTo("admin-secret");
        assertThat(hash).startsWith("$2");
        assertThat(new BCryptPasswordEncoder().matches("admin-secret", hash)).isTrue();
    }

    @Test
    void rejectsBlankPassword() {
        assertThatThrownBy(() -> adapter.hash(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Raw password is required");
    }
}
