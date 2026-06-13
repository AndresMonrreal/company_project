package com.example.company.containers.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ContainerNotFoundException extends DomainException {

    public ContainerNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "container.not-found", "Container not found with id: " + id);
    }
}
