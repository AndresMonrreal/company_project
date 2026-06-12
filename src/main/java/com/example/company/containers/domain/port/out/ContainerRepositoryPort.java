package com.example.company.containers.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.containers.domain.model.Container;

public interface ContainerRepositoryPort {

    List<Container> findAllActiveOrderByCodeAsc();

    Optional<Container> findActiveById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Container save(Container container);
}
