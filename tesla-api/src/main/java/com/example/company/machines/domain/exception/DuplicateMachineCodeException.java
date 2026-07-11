package com.example.company.machines.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class DuplicateMachineCodeException extends DomainException {

    public DuplicateMachineCodeException(String code) {
        super(DomainErrorType.CONFLICT, "machine.duplicate-code", "Machine code already exists: " + code);
    }
}