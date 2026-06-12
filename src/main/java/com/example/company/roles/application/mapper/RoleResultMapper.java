package com.example.company.roles.application.mapper;

import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.in.RoleResult;

public final class RoleResultMapper {

    private RoleResultMapper() {
    }

    public static RoleResult toResult(Role role) {
        return new RoleResult(
                role.id(),
                role.name(),
                role.description(),
                role.active()
        );
    }
}
