package com.example.company.profiles.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ProfileNotFoundException extends DomainException {

    public ProfileNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "profile.not-found", "Profile not found with id: " + id);
    }
}
