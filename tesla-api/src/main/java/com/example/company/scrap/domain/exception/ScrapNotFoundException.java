package com.example.company.scrap.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ScrapNotFoundException extends DomainException {

    public ScrapNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "scrap.not-found", "Scrap record not found: " + id);
    }
}