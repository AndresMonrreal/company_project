package com.example.company.container_types.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.container_types.domain.model.ContainerType;

public interface ContainerTypeRepositoryPort {

    List<ContainerType> findAllActiveOrderByNameAsc();

    Optional<ContainerType> findActiveById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    ContainerType save(ContainerType containerType);
}
