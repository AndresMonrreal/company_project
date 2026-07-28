package com.example.company.inventory.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class InventoryNotAvailableException extends DomainException {

    public InventoryNotAvailableException(String identifier) {
        super(DomainErrorType.NOT_FOUND, "inventory.not-available",
                "No available inventory found for: " + identifier);
    }
}