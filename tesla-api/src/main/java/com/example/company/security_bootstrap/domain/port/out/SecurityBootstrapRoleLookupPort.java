package com.example.company.security_bootstrap.domain.port.out;

import java.util.Optional;

import com.example.company.security_bootstrap.domain.model.BootstrapRoleName;

public interface SecurityBootstrapRoleLookupPort {

    Optional<Long> findRoleIdByName(BootstrapRoleName roleName);
}
