package com.example.company.roles.domain.port.in;

import java.util.List;

public interface GetRoleUseCase {

    List<RoleResult> findAllActive();

    RoleResult findById(Long id);
}
