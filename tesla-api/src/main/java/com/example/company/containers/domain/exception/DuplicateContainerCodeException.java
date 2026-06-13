package com.example.company.containers.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class DuplicateContainerCodeException extends DomainException {

    public DuplicateContainerCodeException(String code) {
        super(DomainErrorType.CONFLICT, "container.duplicate-code", "Container code already exists: " + code);
    }
}
