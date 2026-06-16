package com.example.company.reception.adapter.in.web;

import java.util.List;

import com.example.company.reception.adapter.in.web.dto.ReceptionRequest;
import com.example.company.reception.adapter.in.web.dto.ReceptionResponse;
import com.example.company.reception.domain.port.in.GetReceptionUseCase;
import com.example.company.reception.domain.port.in.RegisterReceptionCommand;
import com.example.company.reception.domain.port.in.RegisterReceptionUseCase;
import com.example.company.reception.domain.port.in.ReceptionResult;
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
@RequestMapping("/api/receptions")
public class ReceptionRestController {

    private final RegisterReceptionUseCase registerReception;
    private final GetReceptionUseCase getReception;

    public ReceptionRestController(RegisterReceptionUseCase registerReception,
                                   GetReceptionUseCase getReception) {
        this.registerReception = registerReception;
        this.getReception = getReception;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceptionResponse register(@Valid @RequestBody ReceptionRequest request,
                                      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        RegisterReceptionCommand command = new RegisterReceptionCommand(
                request.containerId(),
                request.profileId(),
                request.lot(),
                request.receivedQuantity(),
                principal.userId()
        );
        return toResponse(registerReception.register(command));
    }

    @GetMapping("/my")
    public List<ReceptionResponse> findMy(@RequestParam Long shiftId,
                                          @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return getReception.findByOperatorAndShift(principal.userId(), shiftId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ReceptionResponse findById(@PathVariable Long id) {
        return toResponse(getReception.findById(id));
    }

    private ReceptionResponse toResponse(ReceptionResult result) {
        return new ReceptionResponse(
                result.id(),
                result.containerId(),
                result.profileId(),
                result.operatorId(),
                result.lot(),
                result.receivedQuantity(),
                result.status(),
                result.receivedAt()
        );
    }
}