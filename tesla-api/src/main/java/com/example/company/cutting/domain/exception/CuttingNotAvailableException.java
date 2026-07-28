package com.example.company.cutting.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class CuttingNotAvailableException extends DomainException {

    public CuttingNotAvailableException(String identifier) {
        super(DomainErrorType.NOT_FOUND, "cutting.not-available",
                "No available cutting record for: " + identifier);
    }
}