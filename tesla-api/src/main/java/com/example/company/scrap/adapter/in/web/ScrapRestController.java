package com.example.company.scrap.adapter.in.web;

import java.security.Principal;
import java.util.List;

import com.example.company.scrap.adapter.in.web.dto.RegisterScrapRequest;
import com.example.company.scrap.adapter.in.web.dto.ScrapResponse;
import com.example.company.scrap.domain.port.in.GetMyScrapUseCase;
import com.example.company.scrap.domain.port.in.RegisterScrapUseCase;
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
@RequestMapping("/api/scrap")
public class ScrapRestController {

    private final RegisterScrapUseCase registerScrap;
    private final GetMyScrapUseCase getMyScrap;
    private final ScrapWebMapper mapper;

    public ScrapRestController(
            RegisterScrapUseCase registerScrap,
            GetMyScrapUseCase getMyScrap,
            ScrapWebMapper mapper
    ) {
        this.registerScrap = registerScrap;
        this.getMyScrap = getMyScrap;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScrapResponse register(
            @Valid @RequestBody RegisterScrapRequest request,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return mapper.toResponse(registerScrap.register(mapper.toCommand(request, auth.userId())));
    }

    @GetMapping("/{id}")
    public ScrapResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getMyScrap.findById(id));
    }

    @GetMapping("/my")
    public List<ScrapResponse> getMyScrap(
            @RequestParam Long shiftId,
            Principal principal
    ) {
        AuthenticatedUserPrincipal auth = (AuthenticatedUserPrincipal) ((Authentication) principal).getPrincipal();
        return getMyScrap.findByOperatorAndShift(auth.userId(), shiftId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
