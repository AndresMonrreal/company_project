package com.example.company.auth.domain.port.in;

public interface LoginUseCase {

    LoginResult login(LoginCommand command);
}
