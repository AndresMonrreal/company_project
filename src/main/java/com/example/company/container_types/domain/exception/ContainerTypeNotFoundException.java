package com.example.company.container_types.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ContainerTypeNotFoundException extends DomainException {

    public ContainerTypeNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "container-type.not-found", "Container type not found with id: " + id);
    }
}
