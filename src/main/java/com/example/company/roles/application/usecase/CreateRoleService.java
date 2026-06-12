package com.example.company.roles.application.usecase;

import com.example.company.roles.application.mapper.RoleResultMapper;
import com.example.company.roles.domain.exception.DuplicateRoleNameException;
import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.in.CreateRoleCommand;
import com.example.company.roles.domain.port.in.CreateRoleUseCase;
import com.example.company.roles.domain.port.in.RoleResult;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRoleService implements CreateRoleUseCase {

    private final RoleRepositoryPort roleRepository;

    public CreateRoleService(RoleRepositoryPort roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public RoleResult create(CreateRoleCommand command) {
        if (roleRepository.existsByName(command.name())) {
            throw new DuplicateRoleNameException(command.name());
        }

        Role role = Role.create(command.name(), command.description());
        return RoleResultMapper.toResult(roleRepository.save(role));
    }
}
