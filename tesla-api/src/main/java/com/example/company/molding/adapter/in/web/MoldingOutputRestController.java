package com.example.company.molding.adapter.in.web;

import java.security.Principal;
import java.util.List;

import com.example.company.molding.adapter.in.web.dto.MoldingOutputResponse;
import com.example.company.molding.adapter.in.web.dto.RegisterMoldingOutputRequest;
import com.example.company.molding.domain.port.in.GetMyMoldingOutputsUseCase;
import com.example.company.molding.domain.port.in.RegisterMoldingOutputUseCase;
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

@RestController
@RequestMapping("/api/molding-outputs")
public class MoldingOutputRestController {

    private final RegisterMoldingOutputUseCase registerMoldingOutput;
    private final GetMyMoldingOutputsUseCase getMyMoldingOutputs;
    private final MoldingOutputWebMapper mapper;

    public MoldingOutputRestController(
            RegisterMoldingOutputUseCase registerMoldingOutput,
            GetMyMoldingOutputsUseCase getMyMoldingOutputs,
            MoldingOutputWebMapper mapper
    ) {
        this.registerMoldingOutput = registerMoldingOutput;
        this.getMyMoldingOutputs = getMyMoldingOutputs;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MoldingOutputResponse register(
            @Valid @RequestBody RegisterMoldingOutputRequest request,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return mapper.toResponse(registerMoldingOutput.register(mapper.toCommand(request, auth.userId())));
    }

    @GetMapping("/{id}")
    public MoldingOutputResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getMyMoldingOutputs.findById(id));
    }

    @GetMapping("/my")
    public List<MoldingOutputResponse> getMyOutputs(
            @RequestParam Long shiftId,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return getMyMoldingOutputs.findByOperatorAndShift(auth.userId(), shiftId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
