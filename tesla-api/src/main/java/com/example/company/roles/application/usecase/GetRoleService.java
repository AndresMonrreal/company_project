package com.example.company.roles.application.usecase;

import java.util.List;

import com.example.company.roles.application.mapper.RoleResultMapper;
import com.example.company.roles.domain.exception.RoleNotFoundException;
import com.example.company.roles.domain.port.in.GetRoleUseCase;
import com.example.company.roles.domain.port.in.RoleResult;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetRoleService implements GetRoleUseCase {

    private final RoleRepositoryPort roleRepository;

    public GetRoleService(RoleRepositoryPort roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<RoleResult> findAllActive() {
        return roleRepository.findAllActiveOrderByNameAsc()
                .stream()
                .map(RoleResultMapper::toResult)
                .toList();
    }

    @Override
    public RoleResult findById(Long id) {
        return roleRepository.findActiveById(id)
                .map(RoleResultMapper::toResult)
                .orElseThrow(() -> new RoleNotFoundException(id));
    }
}
