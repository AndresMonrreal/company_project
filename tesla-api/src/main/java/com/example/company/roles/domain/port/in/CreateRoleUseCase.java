package com.example.company.roles.domain.port.in;

public interface CreateRoleUseCase {

    RoleResult create(CreateRoleCommand command);
}
