package com.example.company.security_bootstrap.domain.port.in;

import java.util.List;

public record SecurityBootstrapResult(
        boolean skipped,
        int createdUsers,
        int existingUsers,
        boolean demoUsersRequested,
        boolean demoUsersAllowed,
        List<String> createdUsernames,
        List<String> existingUsernames
) {

    public SecurityBootstrapResult {
        createdUsernames = List.copyOf(createdUsernames);
        existingUsernames = List.copyOf(existingUsernames);
    }

    public static SecurityBootstrapResult skippedResult() {
        return new SecurityBootstrapResult(true, 0, 0, false, false, List.of(), List.of());
    }
}
