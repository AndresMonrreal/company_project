package com.example.company.roles.domain.port.in;

public interface UpdateRoleUseCase {

    RoleResult update(Long id, UpdateRoleCommand command);
}
