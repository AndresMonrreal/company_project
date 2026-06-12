package com.example.company.security_bootstrap.domain.port.in;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record SecurityBootstrapCommand(
        boolean enabled,
        String adminUsername,
        String adminFullName,
        String adminPassword,
        boolean demoUsersEnabled,
        Set<String> activeProfiles,
        String supervisorDemoPassword,
        String operadorDemoPassword,
        String consultaDemoPassword
) {

    private static final Set<String> ALLOWED_DEMO_PROFILES = Set.of("local", "dev", "test");

    public SecurityBootstrapCommand {
        adminUsername = defaultText(adminUsername, "admin");
        adminFullName = defaultText(adminFullName, "Initial Administrator");
        activeProfiles = normalizeProfiles(activeProfiles);
    }

    public boolean demoUsersAllowed() {
        return demoUsersEnabled
                && activeProfiles.stream().anyMatch(ALLOWED_DEMO_PROFILES::contains);
    }

    private static String defaultText(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static Set<String> normalizeProfiles(Set<String> activeProfiles) {
        if (activeProfiles == null || activeProfiles.isEmpty()) {
            return Set.of();
        }

        return activeProfiles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(profile -> !profile.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String toString() {
        return "SecurityBootstrapCommand[enabled=%s, adminUsername=%s, adminFullName=%s, adminPassword=<redacted>, demoUsersEnabled=%s, activeProfiles=%s, demoPasswords=<redacted>]"
                .formatted(enabled, adminUsername, adminFullName, demoUsersEnabled, activeProfiles);
    }
}
