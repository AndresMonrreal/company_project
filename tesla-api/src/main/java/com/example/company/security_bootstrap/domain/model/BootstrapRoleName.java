package com.example.company.security_bootstrap.domain.model;

import java.util.List;

public enum BootstrapRoleName {
    ADMIN("ADMIN"),
    SUPERVISOR("SUPERVISOR"),
    OPERADOR("OPERADOR"),
    CONSULTA("CONSULTA");

    private static final List<BootstrapRoleName> REQUIRED_ROLES = List.of(values());

    private final String value;

    BootstrapRoleName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static List<BootstrapRoleName> requiredRoles() {
        return REQUIRED_ROLES;
    }
}
