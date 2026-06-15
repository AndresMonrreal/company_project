package com.example.company.cutting.adapter.in.web;

import com.example.company.cutting.adapter.in.web.dto.CuttingResponse;
import com.example.company.cutting.adapter.in.web.dto.RegisterCutRequest;
import com.example.company.cutting.domain.port.in.GetMyCuttingRecordsUseCase;
import com.example.company.cutting.domain.port.in.RegisterCutUseCase;
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
@RequestMapping("/api/cutting")
public class CuttingRestController {

    private final RegisterCutUseCase registerCut;
    private final GetMyCuttingRecordsUseCase getMyCuttingRecords;
    private final CuttingWebMapper mapper;

    public CuttingRestController(
            RegisterCutUseCase registerCut,
            GetMyCuttingRecordsUseCase getMyCuttingRecords,
            CuttingWebMapper mapper
    ) {
        this.registerCut = registerCut;
        this.getMyCuttingRecords = getMyCuttingRecords;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuttingResponse register(
            @Valid @RequestBody RegisterCutRequest request,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return mapper.toResponse(registerCut.register(mapper.toCommand(request, auth.userId())));
    }

    @GetMapping("/{id}")
    public CuttingResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getMyCuttingRecords.findById(id));
    }

    @GetMapping("/my")
    public List<CuttingResponse> getMyCuttingRecords(
            @RequestParam Long shiftId,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return getMyCuttingRecords.findByOperatorAndShift(auth.userId(), shiftId)
                .stream().map(mapper::toResponse).toList();
    }
}
