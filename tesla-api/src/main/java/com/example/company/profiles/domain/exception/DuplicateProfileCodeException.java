package com.example.company.profiles.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class DuplicateProfileCodeException extends DomainException {

    public DuplicateProfileCodeException(String code) {
        super(DomainErrorType.CONFLICT, "profile.duplicate-code", "Profile code already exists: " + code);
    }
}
