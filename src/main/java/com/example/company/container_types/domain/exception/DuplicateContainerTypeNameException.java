package com.example.company.container_types.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class DuplicateContainerTypeNameException extends DomainException {

    public DuplicateContainerTypeNameException(String name) {
        super(DomainErrorType.CONFLICT, "container-type.duplicate-name", "Container type name already exists: " + name);
    }
}
