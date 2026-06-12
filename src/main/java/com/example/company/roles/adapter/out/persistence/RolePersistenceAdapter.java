package com.example.company.roles.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import com.example.company.roles.domain.model.Role;
import com.example.company.roles.domain.port.out.RoleRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class RolePersistenceAdapter implements RoleRepositoryPort {

    private final SpringDataRoleRepository roleRepository;
    private final RolePersistenceMapper mapper;

    public RolePersistenceAdapter(
            SpringDataRoleRepository roleRepository,
            RolePersistenceMapper mapper
    ) {
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Role> findAllActiveOrderByNameAsc() {
        return roleRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Role> findActiveById(Long id) {
        return roleRepository.findByIdAndActiveTrue(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return roleRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public Role save(Role role) {
        RoleJpaEntity entity = role.id() == null
                ? mapper.toNewEntity(role)
                : roleRepository.findById(role.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Role entity disappeared during save: " + role.id()
                        ));

        entity.updateFromDomain(role.name(), role.description(), role.active());
        return mapper.toDomain(roleRepository.save(entity));
    }
}
