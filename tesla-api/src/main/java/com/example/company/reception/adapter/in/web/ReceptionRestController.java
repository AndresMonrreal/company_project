package com.example.company.reception.adapter.in.web;

import com.example.company.reception.adapter.in.web.dto.RegisterReceptionRequest;
import com.example.company.reception.adapter.in.web.dto.ReceptionResponse;
import com.example.company.reception.domain.port.in.GetMyReceptionsUseCase;
import com.example.company.reception.domain.port.in.RegisterReceptionUseCase;
import com.example.company.security.model.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/receptions")
public class ReceptionRestController {

    private final RegisterReceptionUseCase registerReception;
    private final GetMyReceptionsUseCase getMyReceptions;
    private final ReceptionWebMapper mapper;

    public ReceptionRestController(
            RegisterReceptionUseCase registerReception,
            GetMyReceptionsUseCase getMyReceptions,
            ReceptionWebMapper mapper
    ) {
        this.registerReception = registerReception;
        this.getMyReceptions = getMyReceptions;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceptionResponse register(
            @Valid @RequestBody RegisterReceptionRequest request,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return mapper.toResponse(registerReception.register(mapper.toCommand(request, auth.userId())));
    }

    @GetMapping("/{id}")
    public ReceptionResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getMyReceptions.findById(id));
    }

    @GetMapping("/my")
    public List<ReceptionResponse> getMyReceptions(
            @RequestParam Long shiftId,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return getMyReceptions.findByOperatorAndShift(auth.userId(), shiftId)
                .stream().map(mapper::toResponse).toList();
    }
}
