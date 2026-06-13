package com.example.company.auth.adapter.in.web;

import com.example.company.auth.adapter.in.web.dto.LoginRequest;
import com.example.company.auth.adapter.in.web.dto.LoginResponse;
import com.example.company.auth.domain.port.in.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final LoginUseCase loginUseCase;
    private final AuthWebMapper mapper;

    public AuthRestController(LoginUseCase loginUseCase, AuthWebMapper mapper) {
        this.loginUseCase = loginUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return mapper.toResponse(loginUseCase.login(mapper.toCommand(request)));
    }
}
