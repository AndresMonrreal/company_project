package com.example.company.security_bootstrap.adapter.in.startup;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import com.example.company.security_bootstrap.domain.port.in.SecurityBootstrapCommand;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SecurityBootstrapPropertiesTest {

    @Test
    void buildsDisabledCommandWithoutAdminPassword() {
        SecurityBootstrapProperties properties = new SecurityBootstrapProperties();
        properties.setEnabled(false);

        SecurityBootstrapCommand command = properties.toCommand(Set.of(), new MockEnvironment());

        assertThat(command.enabled()).isFalse();
        assertThat(command.adminPassword()).isNull();
    }

    @Test
    void readsAdminPasswordFromEnvironment() {
        SecurityBootstrapProperties properties = new SecurityBootstrapProperties();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("SECURITY_BOOTSTRAP_ADMIN_PASSWORD", "admin-secret");

        SecurityBootstrapCommand command = properties.toCommand(Set.of(), environment);

        assertThat(command.adminPassword()).isEqualTo("admin-secret");
    }

    @Test
    void usesConfiguredAdminPasswordWhenEnvironmentIsMissing() {
        SecurityBootstrapProperties properties = new SecurityBootstrapProperties();
        properties.getAdmin().setPassword("test-secret");

        SecurityBootstrapCommand command = properties.toCommand(Set.of(), new MockEnvironment());

        assertThat(command.adminPassword()).isEqualTo("test-secret");
    }

    @Test
    void demoUsersAreDisabledByDefault() {
        SecurityBootstrapProperties properties = new SecurityBootstrapProperties();

        SecurityBootstrapCommand command = properties.toCommand(Set.of("test"), new MockEnvironment());

        assertThat(command.demoUsersEnabled()).isFalse();
        assertThat(command.demoUsersAllowed()).isFalse();
    }

    @Test
    void demoUsersRequireEnabledFlagAndAllowedProfile() {
        SecurityBootstrapProperties properties = new SecurityBootstrapProperties();
        properties.getDemoUsers().setEnabled(true);

        SecurityBootstrapCommand prodCommand = properties.toCommand(Set.of("prod"), new MockEnvironment());
        SecurityBootstrapCommand testCommand = properties.toCommand(Set.of("test"), new MockEnvironment());

        assertThat(prodCommand.demoUsersEnabled()).isTrue();
        assertThat(prodCommand.demoUsersAllowed()).isFalse();
        assertThat(testCommand.demoUsersEnabled()).isTrue();
        assertThat(testCommand.demoUsersAllowed()).isTrue();
    }
}
