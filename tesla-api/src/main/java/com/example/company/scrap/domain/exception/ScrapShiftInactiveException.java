package com.example.company.scrap.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ScrapShiftInactiveException extends DomainException {

    public ScrapShiftInactiveException(Long shiftId) {
        super(DomainErrorType.BUSINESS_RULE, "scrap.shift-inactive",
              "Shift " + shiftId + " is not active");
    }
}