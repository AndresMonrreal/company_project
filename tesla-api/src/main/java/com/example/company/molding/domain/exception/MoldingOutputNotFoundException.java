package com.example.company.molding.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class MoldingOutputNotFoundException extends DomainException {

    public MoldingOutputNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "molding.not-found", "Molding output not found: " + id);
    }
}