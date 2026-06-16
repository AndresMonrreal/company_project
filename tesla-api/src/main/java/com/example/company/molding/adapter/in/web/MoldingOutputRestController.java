package com.example.company.molding.adapter.in.web;

import java.util.List;

import com.example.company.molding.adapter.in.web.dto.MoldingOutputRequest;
import com.example.company.molding.adapter.in.web.dto.MoldingOutputResponse;
import com.example.company.molding.domain.port.in.GetMoldingOutputUseCase;
import com.example.company.molding.domain.port.in.MoldingOutputResult;
import com.example.company.molding.domain.port.in.RegisterMoldingOutputCommand;
import com.example.company.molding.domain.port.in.RegisterMoldingOutputUseCase;
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
@RequestMapping("/api/molding-outputs")
public class MoldingOutputRestController {

    private final RegisterMoldingOutputUseCase registerMoldingOutput;
    private final GetMoldingOutputUseCase getMoldingOutput;

    public MoldingOutputRestController(RegisterMoldingOutputUseCase registerMoldingOutput,
                                       GetMoldingOutputUseCase getMoldingOutput) {
        this.registerMoldingOutput = registerMoldingOutput;
        this.getMoldingOutput = getMoldingOutput;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MoldingOutputResponse register(@Valid @RequestBody MoldingOutputRequest request,
                                          @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        RegisterMoldingOutputCommand command = new RegisterMoldingOutputCommand(
                request.cuttingRecordId(),
                request.quantitySent(),
                principal.userId()
        );
        return toResponse(registerMoldingOutput.register(command));
    }

    @GetMapping("/my")
    public List<MoldingOutputResponse> findMy(@RequestParam Long shiftId,
                                              @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return getMoldingOutput.findByOperatorAndShift(principal.userId(), shiftId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public MoldingOutputResponse findById(@PathVariable Long id) {
        return toResponse(getMoldingOutput.findById(id));
    }

    private MoldingOutputResponse toResponse(MoldingOutputResult result) {
        return new MoldingOutputResponse(
                result.id(),
                result.cuttingRecordId(),
                result.quantitySent(),
                result.operatorId(),
                result.sentAt()
        );
    }
}