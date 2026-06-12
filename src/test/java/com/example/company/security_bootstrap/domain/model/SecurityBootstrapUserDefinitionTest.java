package com.example.company.security_bootstrap.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SecurityBootstrapUserDefinitionTest {

    @Test
    void createsDefinitionWithTrimmedValues() {
        BootstrapUserDefinition user = new BootstrapUserDefinition(
                "  admin  ",
                "  Initial Administrator  ",
                BootstrapRoleName.ADMIN,
                "secret",
                false
        );

        assertThat(user.username()).isEqualTo("admin");
        assertThat(user.fullName()).isEqualTo("Initial Administrator");
        assertThat(user.roleName()).isEqualTo(BootstrapRoleName.ADMIN);
        assertThat(user.demo()).isFalse();
    }

    @Test
    void rejectsBlankUsername() {
        assertThatThrownBy(() -> new BootstrapUserDefinition(
                " ",
                "Initial Administrator",
                BootstrapRoleName.ADMIN,
                "secret",
                false
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bootstrap username is required");
    }

    @Test
    void rejectsBlankPassword() {
        assertThatThrownBy(() -> new BootstrapUserDefinition(
                "admin",
                "Initial Administrator",
                BootstrapRoleName.ADMIN,
                " ",
                false
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bootstrap user password is required for admin");
    }

    @Test
    void toStringRedactsRawPassword() {
        BootstrapUserDefinition user = new BootstrapUserDefinition(
                "admin",
                "Initial Administrator",
                BootstrapRoleName.ADMIN,
                "secret",
                false
        );

        assertThat(user.toString()).contains("rawPassword=<redacted>");
        assertThat(user.toString()).doesNotContain("secret");
    }
}
