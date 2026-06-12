package com.example.company.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import com.example.company.auth.adapter.in.web.dto.LoginRequest;
import com.example.company.auth.adapter.in.web.dto.LoginResponse;
import com.example.company.auth.domain.model.AuthenticatedUserSummary;
import com.example.company.auth.domain.port.in.LoginCommand;
import com.example.company.auth.domain.port.in.LoginResult;
import org.junit.jupiter.api.Test;

class AuthWebMapperTest {

    private final AuthWebMapper mapper = new AuthWebMapper();

    @Test
    void mapsLoginRequestToUseCaseCommand() {
        LoginCommand command = mapper.toCommand(new LoginRequest("admin", "Secret123!"));

        assertThat(command.username()).isEqualTo("admin");
        assertThat(command.password()).isEqualTo("Secret123!");
    }

    @Test
    void mapsLoginResultToSafeResponseWithoutPasswordData() {
        LoginResult result = new LoginResult(
                "signed.jwt.token",
                "Bearer",
                Instant.parse("2026-06-12T18:30:00Z"),
                new AuthenticatedUserSummary(1L, "admin", "Initial Administrator", "ADMIN")
        );

        LoginResponse response = mapper.toResponse(result);

        assertThat(response.accessToken()).isEqualTo("signed.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresAt()).isEqualTo(Instant.parse("2026-06-12T18:30:00Z"));
        assertThat(response.user().userId()).isEqualTo(1L);
        assertThat(response.user().username()).isEqualTo("admin");
        assertThat(response.user().fullName()).isEqualTo("Initial Administrator");
        assertThat(response.user().role()).isEqualTo("ADMIN");
        assertThat(response.toString()).doesNotContain("password", "password_hash", "hash");
    }
}
