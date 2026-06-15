package com.example.company.cutting.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class CuttingRecordNotFoundException extends DomainException {

    public CuttingRecordNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND,
                "cutting.not-found",
                "Cutting record not found with id: " + id);
    }
}
