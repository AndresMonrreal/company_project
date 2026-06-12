package com.example.company.roles.application.usecase;

import com.example.company.roles.domain.exception.RoleNotFoundException;
import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.in.DeleteRoleUseCase;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteRoleService implements DeleteRoleUseCase {

    private final RoleRepositoryPort roleRepository;

    public DeleteRoleService(RoleRepositoryPort roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findActiveById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        role.deactivate();
        roleRepository.save(role);
    }
}
