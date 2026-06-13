package com.example.company.roles.application.usecase;

import com.example.company.roles.application.mapper.RoleResultMapper;
import com.example.company.roles.domain.exception.DuplicateRoleNameException;
import com.example.company.roles.domain.exception.RoleNotFoundException;
import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.in.RoleResult;
import com.example.company.roles.domain.port.in.UpdateRoleCommand;
import com.example.company.roles.domain.port.in.UpdateRoleUseCase;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateRoleService implements UpdateRoleUseCase {

    private final RoleRepositoryPort roleRepository;

    public UpdateRoleService(RoleRepositoryPort roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public RoleResult update(Long id, UpdateRoleCommand command) {
        Role role = roleRepository.findActiveById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        if (roleRepository.existsByNameAndIdNot(command.name(), id)) {
            throw new DuplicateRoleNameException(command.name());
        }

        role.update(command.name(), command.description());
        return RoleResultMapper.toResult(roleRepository.save(role));
    }
}
