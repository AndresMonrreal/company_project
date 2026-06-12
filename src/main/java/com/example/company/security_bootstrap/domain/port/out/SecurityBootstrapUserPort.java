package com.example.company.security_bootstrap.domain.port.out;

import com.example.company.security_bootstrap.domain.model.BootstrapUserDefinition;

public interface SecurityBootstrapUserPort {

    boolean existsByUsername(String username);

    void insertBootstrapUser(BootstrapUserDefinition user, Long roleId, String passwordHash);
}
