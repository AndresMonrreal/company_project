package com.example.company.roles.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class RoleNotFoundException extends DomainException {

    public RoleNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "role.not-found", "Role not found: " + id);
    }
}
