package com.example.company.scrap.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class ScrapProfileInactiveException extends DomainException {

    public ScrapProfileInactiveException(Long profileId) {
        super(DomainErrorType.BUSINESS_RULE, "scrap.profile-inactive",
              "Profile " + profileId + " is not active");
    }
}