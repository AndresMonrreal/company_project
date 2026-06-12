package com.example.company.roles.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void createsActiveRoleWithTrimmedNameAndDescription() {
        Role role = Role.create("  ADMIN  ", "  System administrator  ");

        assertThat(role.id()).isNull();
        assertThat(role.name()).isEqualTo("ADMIN");
        assertThat(role.description()).isEqualTo("System administrator");
        assertThat(role.active()).isTrue();
    }

    @Test
    void acceptsNullDescription() {
        Role role = Role.create("ADMIN", null);

        assertThat(role.description()).isNull();
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Role.create(" ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role name is required");
    }

    @Test
    void rejectsTooLongName() {
        String name = "A".repeat(81);

        assertThatThrownBy(() -> Role.create(name, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role name must be 80 characters or fewer");
    }

    @Test
    void rejectsTooLongDescription() {
        String description = "A".repeat(256);

        assertThatThrownBy(() -> Role.create("ADMIN", description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role description must be 255 characters or fewer");
    }

    @Test
    void softDeletesRole() {
        Role role = Role.restore(1L, "ADMIN", "System administrator", true);

        role.deactivate();

        assertThat(role.active()).isFalse();
    }
}
