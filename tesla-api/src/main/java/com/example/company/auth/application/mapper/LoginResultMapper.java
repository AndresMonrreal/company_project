package com.example.company.auth.application.mapper;

import com.example.company.auth.domain.model.AuthenticatedUser;
import com.example.company.auth.domain.model.AuthenticatedUserSummary;
import com.example.company.auth.domain.model.JwtAccessToken;
import com.example.company.auth.domain.port.in.LoginResult;

public final class LoginResultMapper {

    private LoginResultMapper() {
    }

    public static LoginResult toResult(AuthenticatedUser user, JwtAccessToken token) {
        return new LoginResult(
                token.token(),
                "Bearer",
                token.expiresAt(),
                AuthenticatedUserSummary.from(user)
        );
    }
}
