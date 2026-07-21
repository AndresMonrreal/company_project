package com.example.company.containers.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class ContainerPersistenceAdapter implements ContainerRepositoryPort {

    private final SpringDataContainerRepository containerRepository;
    private final ContainerPersistenceMapper mapper;

    public ContainerPersistenceAdapter(SpringDataContainerRepository containerRepository, ContainerPersistenceMapper mapper) {
        this.containerRepository = containerRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Container> findAllActiveOrderByCodeAsc() {
        return containerRepository.findByActiveTrueOrderByCodeAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Container> findActiveById(Long id) {
        return containerRepository.findByIdAndActiveTrue(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return containerRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return containerRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public boolean hasActiveReceptions(Long containerId) {
        return containerRepository.hasActiveReceptions(containerId);
    }

    @Override
    public Container save(Container container) {
        ContainerJpaEntity entity = container.id() == null
                ? mapper.toNewEntity(container)
                : containerRepository.findById(container.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Container entity disappeared during save: " + container.id()
                        ));

        entity.updateFromDomain(container.containerTypeId(), container.code(), container.active());
        return mapper.toDomain(containerRepository.save(entity));
    }
}
