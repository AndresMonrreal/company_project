package com.example.company.shifts.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class DuplicateShiftNameException extends DomainException {

    public DuplicateShiftNameException(String name) {
        super(DomainErrorType.CONFLICT, "shift.duplicate-name", "Shift name already exists: " + name);
    }
}
