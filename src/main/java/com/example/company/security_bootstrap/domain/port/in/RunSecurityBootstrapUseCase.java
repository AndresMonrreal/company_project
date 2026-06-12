package com.example.company.security_bootstrap.domain.port.in;

public interface RunSecurityBootstrapUseCase {

    SecurityBootstrapResult run(SecurityBootstrapCommand command);
}
