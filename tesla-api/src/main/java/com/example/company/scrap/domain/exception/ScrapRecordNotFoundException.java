package com.example.company.scrap.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ScrapRecordNotFoundException extends DomainException {

    public ScrapRecordNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND,
                "scrap.not-found",
                "Scrap record not found with id: " + id);
    }
}
