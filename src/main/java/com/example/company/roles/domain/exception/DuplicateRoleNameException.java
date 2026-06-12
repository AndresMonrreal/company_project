package com.example.company.roles.domain.exception;

import com.example.company.shared.domain.exception.DomainErrorType;
import com.example.company.shared.domain.exception.DomainException;

public class DuplicateRoleNameException extends DomainException {

    public DuplicateRoleNameException(String name) {
        super(DomainErrorType.CONFLICT, "role.duplicate-name", "Role name already exists: " + name);
    }
}
