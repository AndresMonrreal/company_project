package com.example.company.reception.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ReceptionNotFoundException extends DomainException {

    public ReceptionNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND,
                "reception.not-found",
                "Reception not found with id: " + id);
    }
}
