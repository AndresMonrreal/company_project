package com.example.company.security_bootstrap.application.usecase;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.company.security_bootstrap.domain.model.BootstrapRoleName;
import com.example.company.security_bootstrap.domain.model.BootstrapUserDefinition;
import com.example.company.security_bootstrap.domain.port.in.RunSecurityBootstrapUseCase;
import com.example.company.security_bootstrap.domain.port.in.SecurityBootstrapCommand;
import com.example.company.security_bootstrap.domain.port.in.SecurityBootstrapResult;
import com.example.company.security_bootstrap.domain.port.out.PasswordHashingPort;
import com.example.company.security_bootstrap.domain.port.out.SecurityBootstrapRoleLookupPort;
import com.example.company.security_bootstrap.domain.port.out.SecurityBootstrapUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityBootstrapService implements RunSecurityBootstrapUseCase {

    private final SecurityBootstrapRoleLookupPort roleLookupPort;
    private final SecurityBootstrapUserPort userPort;
    private final PasswordHashingPort passwordHashingPort;

    public SecurityBootstrapService(
            SecurityBootstrapRoleLookupPort roleLookupPort,
            SecurityBootstrapUserPort userPort,
            PasswordHashingPort passwordHashingPort
    ) {
        this.roleLookupPort = roleLookupPort;
        this.userPort = userPort;
        this.passwordHashingPort = passwordHashingPort;
    }

    @Override
    @Transactional
    public SecurityBootstrapResult run(SecurityBootstrapCommand command) {
        if (!command.enabled()) {
            return SecurityBootstrapResult.skippedResult();
        }

        Map<BootstrapRoleName, Long> roleIds = resolveRequiredRoleIds();
        List<String> createdUsernames = new ArrayList<>();
        List<String> existingUsernames = new ArrayList<>();

        createUserIfMissing(
                command.adminUsername(),
                command.adminFullName(),
                BootstrapRoleName.ADMIN,
                command.adminPassword(),
                false,
                roleIds,
                createdUsernames,
                existingUsernames
        );

        if (command.demoUsersAllowed()) {
            createUserIfMissing(
                    command.supervisorDemoUsername(),
                    command.supervisorDemoFullName(),
                    BootstrapRoleName.SUPERVISOR,
                    command.supervisorDemoPassword(),
                    true,
                    roleIds,
                    createdUsernames,
                    existingUsernames
            );
            createUserIfMissing(
                    command.operadorDemoUsername(),
                    command.operadorDemoFullName(),
                    BootstrapRoleName.OPERADOR,
                    command.operadorDemoPassword(),
                    true,
                    roleIds,
                    createdUsernames,
                    existingUsernames
            );
            createUserIfMissing(
                    command.consultaDemoUsername(),
                    command.consultaDemoFullName(),
                    BootstrapRoleName.CONSULTA,
                    command.consultaDemoPassword(),
                    true,
                    roleIds,
                    createdUsernames,
                    existingUsernames
            );
        }

        return new SecurityBootstrapResult(
                false,
                createdUsernames.size(),
                existingUsernames.size(),
                command.demoUsersEnabled(),
                command.demoUsersAllowed(),
                createdUsernames,
                existingUsernames
        );
    }

    private Map<BootstrapRoleName, Long> resolveRequiredRoleIds() {
        Map<BootstrapRoleName, Long> roleIds = new EnumMap<>(BootstrapRoleName.class);
        List<String> missingRoles = new ArrayList<>();

        for (BootstrapRoleName roleName : BootstrapRoleName.requiredRoles()) {
            roleLookupPort.findRoleIdByName(roleName)
                    .ifPresentOrElse(
                            roleId -> roleIds.put(roleName, roleId),
                            () -> missingRoles.add(roleName.value())
                    );
        }

        if (!missingRoles.isEmpty()) {
            throw new IllegalStateException(
                    "Missing required security bootstrap roles: " + String.join(", ", missingRoles)
            );
        }

        return roleIds;
    }

    private void createUserIfMissing(
            String username,
            String fullName,
            BootstrapRoleName roleName,
            String rawPassword,
            boolean demo,
            Map<BootstrapRoleName, Long> roleIds,
            List<String> createdUsernames,
            List<String> existingUsernames
    ) {
        if (userPort.existsByUsername(username)) {
            existingUsernames.add(username);
            return;
        }

        requirePassword(username, rawPassword, demo);

        BootstrapUserDefinition user = new BootstrapUserDefinition(
                username,
                fullName,
                roleName,
                rawPassword,
                demo
        );
        String passwordHash = passwordHashingPort.hash(user.rawPassword());

        userPort.insertBootstrapUser(user, roleIds.get(roleName), passwordHash);
        createdUsernames.add(user.username());
    }

    private void requirePassword(String username, String rawPassword, boolean demo) {
        if (rawPassword != null && !rawPassword.isBlank()) {
            return;
        }

        if (demo) {
            throw new IllegalStateException("Demo bootstrap password is required for " + username);
        }

        throw new IllegalStateException(
                "SECURITY_BOOTSTRAP_ADMIN_PASSWORD is required when security bootstrap is enabled and admin user does not exist"
        );
    }
}
