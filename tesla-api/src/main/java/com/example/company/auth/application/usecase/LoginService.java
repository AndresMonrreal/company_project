package com.example.company.auth.application.usecase;

import com.example.company.auth.application.mapper.LoginResultMapper;
import com.example.company.auth.domain.exception.InactiveRoleException;
import com.example.company.auth.domain.exception.InactiveUserException;
import com.example.company.auth.domain.exception.InvalidCredentialsException;
import com.example.company.auth.domain.exception.RoleUnavailableException;
import com.example.company.auth.domain.model.AuthUserRecord;
import com.example.company.auth.domain.model.AuthenticatedUser;
import com.example.company.auth.domain.model.JwtAccessToken;
import com.example.company.auth.domain.model.LoginCredentials;
import com.example.company.auth.domain.port.in.LoginCommand;
import com.example.company.auth.domain.port.in.LoginResult;
import com.example.company.auth.domain.port.in.LoginUseCase;
import com.example.company.auth.domain.port.out.AuthUserLookupPort;
import com.example.company.auth.domain.port.out.JwtTokenPort;
import com.example.company.auth.domain.port.out.PasswordVerificationPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService implements LoginUseCase {

    private final AuthUserLookupPort userLookupPort;
    private final PasswordVerificationPort passwordVerificationPort;
    private final JwtTokenPort jwtTokenPort;

    public LoginService(
            AuthUserLookupPort userLookupPort,
            PasswordVerificationPort passwordVerificationPort,
            JwtTokenPort jwtTokenPort
    ) {
        this.userLookupPort = userLookupPort;
        this.passwordVerificationPort = passwordVerificationPort;
        this.jwtTokenPort = jwtTokenPort;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        LoginCredentials credentials = new LoginCredentials(command.username(), command.password());

        AuthUserRecord userRecord = userLookupPort.findByUsername(credentials.username())
                // Username and password failures intentionally share one error so login cannot reveal which value was wrong.
                .orElseThrow(InvalidCredentialsException::new);

        if (!userRecord.active()) {
            throw new InactiveUserException();
        }
        if (!userRecord.hasRole()) {
            throw new RoleUnavailableException();
        }
        if (!Boolean.TRUE.equals(userRecord.roleActive())) {
            throw new InactiveRoleException();
        }
        if (!passwordVerificationPort.matches(credentials.password(), userRecord.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        AuthenticatedUser user = new AuthenticatedUser(
                userRecord.userId(),
                userRecord.username(),
                userRecord.fullName(),
                userRecord.roleName()
        );
        JwtAccessToken accessToken = jwtTokenPort.generate(user);

        return LoginResultMapper.toResult(user, accessToken);
    }
}
