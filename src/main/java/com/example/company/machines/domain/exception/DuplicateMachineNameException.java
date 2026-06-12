package com.example.company.machines.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class DuplicateMachineNameException extends DomainException {

    public DuplicateMachineNameException(String name) {
        super(DomainErrorType.CONFLICT, "machine.duplicate-name", "Machine name already exists: " + name);
    }
}
