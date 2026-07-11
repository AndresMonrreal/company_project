package com.example.company.scrap.adapter.in.web;

import java.util.List;

import com.example.company.scrap.adapter.in.web.dto.ScrapRequest;
import com.example.company.scrap.adapter.in.web.dto.ScrapResponse;
import com.example.company.scrap.domain.port.in.GetScrapUseCase;
import com.example.company.scrap.domain.port.in.RegisterScrapCommand;
import com.example.company.scrap.domain.port.in.RegisterScrapUseCase;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.security.model.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final GetScrapUseCase getScrap;

    public ScrapRestController(RegisterScrapUseCase registerScrap, GetScrapUseCase getScrap) {
        this.registerScrap = registerScrap;
        this.getScrap = getScrap;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScrapResponse register(@Valid @RequestBody ScrapRequest request,
                                  @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        RegisterScrapCommand command = new RegisterScrapCommand(
                request.shiftId(),
                request.profileId(),
                principal.userId(),
                request.quantity(),
                request.reason()
        );
        return toResponse(registerScrap.register(command));
    }

    @GetMapping("/my")
    public List<ScrapResponse> findMy(@RequestParam Long shiftId,
                                      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return getScrap.findByOperatorAndShift(principal.userId(), shiftId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @GetMapping("/{id}")
    public ScrapResponse findById(@PathVariable Long id) {
        return toResponse(getScrap.findById(id));
    }

    private ScrapResponse toResponse(ScrapResult result) {
        return new ScrapResponse(
                result.id(),
                result.shiftId(),
                result.shiftName(),
                result.profileId(),
                result.profileCode(),
                result.operatorId(),
                result.quantity(),
                result.reason(),
                result.createdAt()
        );
    }
}