package com.example.company.security_bootstrap.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.example.company.security_bootstrap.domain.model.BootstrapRoleName;
import com.example.company.security_bootstrap.domain.model.BootstrapUserDefinition;
import com.example.company.security_bootstrap.domain.port.in.SecurityBootstrapCommand;
import com.example.company.security_bootstrap.domain.port.in.SecurityBootstrapResult;
import com.example.company.security_bootstrap.domain.port.out.PasswordHashingPort;
import com.example.company.security_bootstrap.domain.port.out.SecurityBootstrapRoleLookupPort;
import com.example.company.security_bootstrap.domain.port.out.SecurityBootstrapUserPort;
import org.junit.jupiter.api.Test;

class SecurityBootstrapServiceTest {

    @Test
    void disabledBootstrapSkipsWithoutPasswordOrRoleLookup() {
        FakeUserPort userPort = new FakeUserPort();
        FakePasswordHashingPort passwordHashingPort = new FakePasswordHashingPort();
        SecurityBootstrapService service = new SecurityBootstrapService(
                roleName -> Optional.empty(),
                userPort,
                passwordHashingPort
        );

        SecurityBootstrapResult result = service.run(new SecurityBootstrapCommand(
                false,
                "admin",
                "Initial Administrator",
                null,
                true,
                Set.of("test"),
                "supervisor",
                "Demo Supervisor",
                null,
                "operador",
                "Demo Operator",
                null,
                "consulta",
                "Demo Consulta",
                null
        ));

        assertThat(result.skipped()).isTrue();
        assertThat(userPort.inserted).isEmpty();
        assertThat(passwordHashingPort.rawPasswords).isEmpty();
    }

    @Test
    void failsWhenRequiredRoleIsMissingBeforeCreatingUsers() {
        FakeRoleLookupPort roles = FakeRoleLookupPort.withAllRoles();
        roles.roleIds.remove(BootstrapRoleName.OPERADOR);
        FakeUserPort userPort = new FakeUserPort();
        FakePasswordHashingPort passwordHashingPort = new FakePasswordHashingPort();
        SecurityBootstrapService service = new SecurityBootstrapService(roles, userPort, passwordHashingPort);

        assertThatThrownBy(() -> service.run(command("admin-secret", false, Set.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing required security bootstrap roles")
                .hasMessageContaining("OPERADOR");

        assertThat(userPort.inserted).isEmpty();
        assertThat(passwordHashingPort.rawPasswords).isEmpty();
    }

    @Test
    void doesNotOverwriteExistingAdminAndDoesNotRequirePassword() {
        FakeUserPort userPort = new FakeUserPort();
        userPort.existing.add("admin");
        FakePasswordHashingPort passwordHashingPort = new FakePasswordHashingPort();
        SecurityBootstrapService service = new SecurityBootstrapService(
                FakeRoleLookupPort.withAllRoles(),
                userPort,
                passwordHashingPort
        );

        SecurityBootstrapResult result = service.run(command(null, false, Set.of()));

        assertThat(result.createdUsers()).isZero();
        assertThat(result.existingUsernames()).containsExactly("admin");
        assertThat(userPort.inserted).isEmpty();
        assertThat(passwordHashingPort.rawPasswords).isEmpty();
    }

    @Test
    void requiresAdminPasswordWhenAdminDoesNotExist() {
        FakeUserPort userPort = new FakeUserPort();
        FakePasswordHashingPort passwordHashingPort = new FakePasswordHashingPort();
        SecurityBootstrapService service = new SecurityBootstrapService(
                FakeRoleLookupPort.withAllRoles(),
                userPort,
                passwordHashingPort
        );

        assertThatThrownBy(() -> service.run(command(null, false, Set.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("SECURITY_BOOTSTRAP_ADMIN_PASSWORD is required when security bootstrap is enabled and admin user does not exist");

        assertThat(userPort.inserted).isEmpty();
        assertThat(passwordHashingPort.rawPasswords).isEmpty();
    }

    @Test
    void createsAdminWithHashedPassword() {
        FakeUserPort userPort = new FakeUserPort();
        FakePasswordHashingPort passwordHashingPort = new FakePasswordHashingPort();
        SecurityBootstrapService service = new SecurityBootstrapService(
                FakeRoleLookupPort.withAllRoles(),
                userPort,
                passwordHashingPort
        );

        SecurityBootstrapResult result = service.run(command("admin-secret", false, Set.of()));

        assertThat(result.createdUsernames()).containsExactly("admin");
        assertThat(userPort.inserted).singleElement().satisfies(savedUser -> {
            assertThat(savedUser.username()).isEqualTo("admin");
            assertThat(savedUser.roleName()).isEqualTo(BootstrapRoleName.ADMIN);
            assertThat(savedUser.roleId()).isEqualTo(1L);
            assertThat(savedUser.passwordHash()).isEqualTo("hashed:admin-secret");
        });
        assertThat(passwordHashingPort.rawPasswords).containsExactly("admin-secret");
    }

    @Test
    void skipsDemoUsersByDefault() {
        FakeUserPort userPort = new FakeUserPort();
        SecurityBootstrapService service = new SecurityBootstrapService(
                FakeRoleLookupPort.withAllRoles(),
                userPort,
                new FakePasswordHashingPort()
        );

        SecurityBootstrapResult result = service.run(command("admin-secret", false, Set.of("test")));

        assertThat(result.demoUsersRequested()).isFalse();
        assertThat(result.demoUsersAllowed()).isFalse();
        assertThat(userPort.inserted).extracting(SavedUser::username).containsExactly("admin");
    }

    @Test
    void demoUsersRequireEnabledFlagAndAllowedProfile() {
        FakeUserPort productionUserPort = new FakeUserPort();
        SecurityBootstrapService productionService = new SecurityBootstrapService(
                FakeRoleLookupPort.withAllRoles(),
                productionUserPort,
                new FakePasswordHashingPort()
        );

        SecurityBootstrapResult productionResult = productionService.run(command("admin-secret", true, Set.of("prod")));

        assertThat(productionResult.demoUsersRequested()).isTrue();
        assertThat(productionResult.demoUsersAllowed()).isFalse();
        assertThat(productionUserPort.inserted).extracting(SavedUser::username).containsExactly("admin");

        FakeUserPort localUserPort = new FakeUserPort();
        SecurityBootstrapService localService = new SecurityBootstrapService(
                FakeRoleLookupPort.withAllRoles(),
                localUserPort,
                new FakePasswordHashingPort()
        );

        SecurityBootstrapResult localResult = localService.run(command("admin-secret", true, Set.of("local")));

        assertThat(localResult.demoUsersRequested()).isTrue();
        assertThat(localResult.demoUsersAllowed()).isTrue();
        assertThat(localUserPort.inserted).extracting(SavedUser::username)
                .containsExactly("admin", "supervisor", "operador", "consulta");
    }

    @Test
    void doesNotOverwriteExistingDemoUsers() {
        FakeUserPort userPort = new FakeUserPort();
        userPort.existing.add("supervisor");
        SecurityBootstrapService service = new SecurityBootstrapService(
                FakeRoleLookupPort.withAllRoles(),
                userPort,
                new FakePasswordHashingPort()
        );

        SecurityBootstrapResult result = service.run(command("admin-secret", true, Set.of("test")));

        assertThat(result.existingUsernames()).containsExactly("supervisor");
        assertThat(userPort.inserted).extracting(SavedUser::username)
                .containsExactly("admin", "operador", "consulta");
    }

    @Test
    void resultAndCommandDoNotExposeRawPasswords() {
        SecurityBootstrapCommand command = command("admin-secret", true, Set.of("test"));
        FakeUserPort userPort = new FakeUserPort();
        SecurityBootstrapService service = new SecurityBootstrapService(
                FakeRoleLookupPort.withAllRoles(),
                userPort,
                new FakePasswordHashingPort()
        );

        SecurityBootstrapResult result = service.run(command);

        assertThat(command.toString())
                .doesNotContain("admin-secret")
                .doesNotContain("supervisor-secret")
                .doesNotContain("operador-secret")
                .doesNotContain("consulta-secret");
        assertThat(result.toString())
                .doesNotContain("admin-secret")
                .doesNotContain("supervisor-secret")
                .doesNotContain("operador-secret")
                .doesNotContain("consulta-secret");
    }

    private static SecurityBootstrapCommand command(
            String adminPassword,
            boolean demoUsersEnabled,
            Set<String> activeProfiles
    ) {
        return new SecurityBootstrapCommand(
                true,
                "admin",
                "Initial Administrator",
                adminPassword,
                demoUsersEnabled,
                activeProfiles,
                "supervisor",
                "Demo Supervisor",
                "supervisor-secret",
                "operador",
                "Demo Operator",
                "operador-secret",
                "consulta",
                "Demo Consulta",
                "consulta-secret"
        );
    }

    private static class FakeRoleLookupPort implements SecurityBootstrapRoleLookupPort {

        private final Map<BootstrapRoleName, Long> roleIds = new EnumMap<>(BootstrapRoleName.class);

        static FakeRoleLookupPort withAllRoles() {
            FakeRoleLookupPort lookupPort = new FakeRoleLookupPort();
            lookupPort.roleIds.put(BootstrapRoleName.ADMIN, 1L);
            lookupPort.roleIds.put(BootstrapRoleName.SUPERVISOR, 2L);
            lookupPort.roleIds.put(BootstrapRoleName.OPERADOR, 3L);
            lookupPort.roleIds.put(BootstrapRoleName.CONSULTA, 4L);
            return lookupPort;
        }

        @Override
        public Optional<Long> findRoleIdByName(BootstrapRoleName roleName) {
            return Optional.ofNullable(roleIds.get(roleName));
        }
    }

    private static class FakeUserPort implements SecurityBootstrapUserPort {

        private final Set<String> existing = new HashSet<>();
        private final List<SavedUser> inserted = new ArrayList<>();

        @Override
        public boolean existsByUsername(String username) {
            return existing.contains(username);
        }

        @Override
        public void insertBootstrapUser(BootstrapUserDefinition user, Long roleId, String passwordHash) {
            inserted.add(new SavedUser(
                    user.username(),
                    user.fullName(),
                    user.roleName(),
                    roleId,
                    passwordHash,
                    user.demo()
            ));
            existing.add(user.username());
        }
    }

    private static class FakePasswordHashingPort implements PasswordHashingPort {

        private final List<String> rawPasswords = new ArrayList<>();

        @Override
        public String hash(String rawPassword) {
            rawPasswords.add(rawPassword);
            return "hashed:" + rawPassword;
        }
    }

    private record SavedUser(
            String username,
            String fullName,
            BootstrapRoleName roleName,
            Long roleId,
            String passwordHash,
            boolean demo
    ) {
    }
}
