package com.example.company.container_types.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class ContainerTypePersistenceAdapter implements ContainerTypeRepositoryPort {

    private final SpringDataContainerTypeRepository containerTypeRepository;
    private final ContainerTypePersistenceMapper mapper;

    public ContainerTypePersistenceAdapter(
            SpringDataContainerTypeRepository containerTypeRepository,
            ContainerTypePersistenceMapper mapper
    ) {
        this.containerTypeRepository = containerTypeRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ContainerType> findAllActiveOrderByNameAsc() {
        return containerTypeRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ContainerType> findActiveById(Long id) {
        return containerTypeRepository.findByIdAndActiveTrue(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return containerTypeRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return containerTypeRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public ContainerType save(ContainerType containerType) {
        ContainerTypeJpaEntity entity = containerType.id() == null
                ? mapper.toNewEntity(containerType)
                : containerTypeRepository.findById(containerType.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Container type entity disappeared during save: " + containerType.id()
                        ));

        entity.updateFromDomain(containerType.name(), containerType.active());
        return mapper.toDomain(containerTypeRepository.save(entity));
    }
}
