package com.example.company.auth.adapter.in.web;

import com.example.company.auth.adapter.in.web.dto.AuthenticatedUserResponse;
import com.example.company.auth.adapter.in.web.dto.LoginRequest;
import com.example.company.auth.adapter.in.web.dto.LoginResponse;
import com.example.company.auth.domain.model.AuthenticatedUserSummary;
import com.example.company.auth.domain.port.in.LoginCommand;
import com.example.company.auth.domain.port.in.LoginResult;
import org.springframework.stereotype.Component;

@Component
public class AuthWebMapper {

    public LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.username(), request.password());
    }

    public LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresAt(),
                toResponse(result.user())
        );
    }

    private AuthenticatedUserResponse toResponse(AuthenticatedUserSummary user) {
        return new AuthenticatedUserResponse(
                user.userId(),
                user.username(),
                user.fullName(),
                user.role()
        );
    }
}
