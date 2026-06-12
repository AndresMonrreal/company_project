package com.example.company.machines.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class MachineNotFoundException extends DomainException {

    public MachineNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "machine.not-found", "Machine not found: " + id);
    }
}
