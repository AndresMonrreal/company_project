package com.example.company.shifts.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ShiftNotFoundException extends DomainException {

    public ShiftNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "shift.not-found", "Shift not found: " + id);
    }
}
