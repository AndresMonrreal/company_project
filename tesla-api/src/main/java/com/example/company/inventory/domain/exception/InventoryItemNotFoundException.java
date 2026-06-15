package com.example.company.inventory.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public final class InventoryItemNotFoundException extends DomainException {

    public InventoryItemNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "inventory.not-found", "Inventory item not found: " + id);
    }
}
