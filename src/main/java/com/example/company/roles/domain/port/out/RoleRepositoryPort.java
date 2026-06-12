package com.example.company.roles.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.roles.domain.model.Role;

public interface RoleRepositoryPort {

    List<Role> findAllActiveOrderByNameAsc();

    Optional<Role> findActiveById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Role save(Role role);
}
