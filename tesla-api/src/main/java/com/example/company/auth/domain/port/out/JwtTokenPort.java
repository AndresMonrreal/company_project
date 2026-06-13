package com.example.company.auth.domain.port.out;

import com.example.company.auth.domain.model.AuthenticatedUser;
import com.example.company.auth.domain.model.JwtAccessToken;

public interface JwtTokenPort {

    JwtAccessToken generate(AuthenticatedUser user);
}
