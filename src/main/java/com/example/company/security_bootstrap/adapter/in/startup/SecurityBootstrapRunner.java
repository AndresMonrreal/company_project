package com.example.company.security_bootstrap.adapter.in.startup;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.company.security_bootstrap.domain.port.in.RunSecurityBootstrapUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SecurityBootstrapRunner implements ApplicationRunner {

    private final RunSecurityBootstrapUseCase securityBootstrapUseCase;
    private final SecurityBootstrapProperties properties;
    private final Environment environment;

    public SecurityBootstrapRunner(
            RunSecurityBootstrapUseCase securityBootstrapUseCase,
            SecurityBootstrapProperties properties,
            Environment environment
    ) {
        this.securityBootstrapUseCase = securityBootstrapUseCase;
        this.properties = properties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        securityBootstrapUseCase.run(properties.toCommand(activeProfiles(), environment));
    }

    private Set<String> activeProfiles() {
        return Arrays.stream(environment.getActiveProfiles())
                .collect(Collectors.toUnmodifiableSet());
    }
}
