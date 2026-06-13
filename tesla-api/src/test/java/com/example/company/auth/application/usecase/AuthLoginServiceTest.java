package com.example.company.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;

import com.example.company.auth.domain.exception.InactiveRoleException;
import com.example.company.auth.domain.exception.InactiveUserException;
import com.example.company.auth.domain.exception.InvalidCredentialsException;
import com.example.company.auth.domain.exception.RoleUnavailableException;
import com.example.company.auth.domain.model.AuthUserRecord;
import com.example.company.auth.domain.model.AuthenticatedUser;
import com.example.company.auth.domain.model.JwtAccessToken;
import com.example.company.auth.domain.port.in.LoginCommand;
import com.example.company.auth.domain.port.in.LoginResult;
import com.example.company.auth.domain.port.out.AuthUserLookupPort;
import com.example.company.auth.domain.port.out.JwtTokenPort;
import com.example.company.auth.domain.port.out.PasswordVerificationPort;
import org.junit.jupiter.api.Test;

class AuthLoginServiceTest {

    private static final Instant EXPIRES_AT = Instant.parse("2026-06-12T18:30:00Z");

    @Test
    void activeUserCanLoginWithMatchingPassword() {
        LoginService service = serviceWith(
                Optional.of(activeAdmin()),
                (rawPassword, passwordHash) -> rawPassword.equals("Secret123!") && passwordHash.equals("$2a$hash"),
                user -> new JwtAccessToken("signed.jwt.token", EXPIRES_AT)
        );

        LoginResult result = service.login(new LoginCommand("admin", "Secret123!"));

        assertThat(result.accessToken()).isEqualTo("signed.jwt.token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(result.user().userId()).isEqualTo(1L);
        assertThat(result.user().username()).isEqualTo("admin");
        assertThat(result.user().fullName()).isEqualTo("Initial Administrator");
        assertThat(result.user().role()).isEqualTo("ADMIN");
    }

    @Test
    void unknownUsernameUsesGenericInvalidCredentialsError() {
        LoginService service = serviceWith(Optional.empty(), (rawPassword, passwordHash) -> true, unusedTokenPort());

        assertThatThrownBy(() -> service.login(new LoginCommand("missing", "Secret123!")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password")
                .satisfies(ex -> assertThat(((InvalidCredentialsException) ex).code())
                        .isEqualTo("auth.invalid-credentials"));
    }

    @Test
    void wrongPasswordUsesSameGenericInvalidCredentialsError() {
        LoginService service = serviceWith(Optional.of(activeAdmin()), (rawPassword, passwordHash) -> false, unusedTokenPort());

        assertThatThrownBy(() -> service.login(new LoginCommand("admin", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password")
                .satisfies(ex -> assertThat(((InvalidCredentialsException) ex).code())
                        .isEqualTo("auth.invalid-credentials"));
    }

    @Test
    void inactiveUserIsRejectedBeforePasswordMatch() {
        AuthUserRecord inactiveUser = new AuthUserRecord(
                1L,
                "admin",
                "Initial Administrator",
                "$2a$hash",
                false,
                "ADMIN",
                true
        );
        LoginService service = serviceWith(Optional.of(inactiveUser), (rawPassword, passwordHash) -> true, unusedTokenPort());

        assertThatThrownBy(() -> service.login(new LoginCommand("admin", "Secret123!")))
                .isInstanceOf(InactiveUserException.class)
                .satisfies(ex -> assertThat(((InactiveUserException) ex).code())
                        .isEqualTo("auth.inactive-user"));
    }

    @Test
    void missingRoleIsRejected() {
        AuthUserRecord missingRole = new AuthUserRecord(
                1L,
                "admin",
                "Initial Administrator",
                "$2a$hash",
                true,
                null,
                null
        );
        LoginService service = serviceWith(Optional.of(missingRole), (rawPassword, passwordHash) -> true, unusedTokenPort());

        assertThatThrownBy(() -> service.login(new LoginCommand("admin", "Secret123!")))
                .isInstanceOf(RoleUnavailableException.class)
                .satisfies(ex -> assertThat(((RoleUnavailableException) ex).code())
                        .isEqualTo("auth.role-unavailable"));
    }

    @Test
    void inactiveRoleIsRejected() {
        AuthUserRecord inactiveRole = new AuthUserRecord(
                1L,
                "admin",
                "Initial Administrator",
                "$2a$hash",
                true,
                "ADMIN",
                false
        );
        LoginService service = serviceWith(Optional.of(inactiveRole), (rawPassword, passwordHash) -> true, unusedTokenPort());

        assertThatThrownBy(() -> service.login(new LoginCommand("admin", "Secret123!")))
                .isInstanceOf(InactiveRoleException.class)
                .satisfies(ex -> assertThat(((InactiveRoleException) ex).code())
                        .isEqualTo("auth.inactive-role"));
    }

    private LoginService serviceWith(
            Optional<AuthUserRecord> userRecord,
            PasswordVerificationPort passwordVerificationPort,
            JwtTokenPort jwtTokenPort
    ) {
        AuthUserLookupPort userLookupPort = username -> userRecord;
        return new LoginService(userLookupPort, passwordVerificationPort, jwtTokenPort);
    }

    private AuthUserRecord activeAdmin() {
        return new AuthUserRecord(
                1L,
                "admin",
                "Initial Administrator",
                "$2a$hash",
                true,
                "ADMIN",
                true
        );
    }

    private JwtTokenPort unusedTokenPort() {
        return user -> {
            throw new AssertionError("JWT generation should not run for rejected login");
        };
    }
}
