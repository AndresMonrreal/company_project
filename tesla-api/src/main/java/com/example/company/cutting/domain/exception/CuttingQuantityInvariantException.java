package com.example.company.cutting.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class CuttingQuantityInvariantException extends DomainException {

    public CuttingQuantityInvariantException(String message) {
        super(DomainErrorType.BUSINESS_RULE, "cutting.quantity-invariant", message);
    }
}