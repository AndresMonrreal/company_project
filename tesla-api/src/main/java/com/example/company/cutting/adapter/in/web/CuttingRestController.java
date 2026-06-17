package com.example.company.cutting.adapter.in.web;

import java.util.List;

import com.example.company.cutting.adapter.in.web.dto.AvailableCuttingResponse;
import com.example.company.cutting.adapter.in.web.dto.CuttingRequest;
import com.example.company.cutting.adapter.in.web.dto.CuttingResponse;
import com.example.company.cutting.domain.model.AvailableCuttingResult;
import com.example.company.cutting.domain.port.in.CuttingResult;
import com.example.company.cutting.domain.port.in.GetCuttingUseCase;
import com.example.company.cutting.domain.port.in.RegisterCuttingCommand;
import com.example.company.cutting.domain.port.in.RegisterCuttingUseCase;
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
@RequestMapping("/api/cutting")
public class CuttingRestController {

    private final RegisterCuttingUseCase registerCutting;
    private final GetCuttingUseCase getCutting;

    public CuttingRestController(RegisterCuttingUseCase registerCutting, GetCuttingUseCase getCutting) {
        this.registerCutting = registerCutting;
        this.getCutting = getCutting;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuttingResponse register(@Valid @RequestBody CuttingRequest request,
                                    @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        RegisterCuttingCommand command = new RegisterCuttingCommand(
                request.inventoryItemId(),
                request.machineId(),
                request.shiftId(),
                request.initialQuantity(),
                request.goodQuantity(),
                request.scrapQuantity(),
                principal.userId()
        );
        return toResponse(registerCutting.register(command));
    }

    @GetMapping("/my")
    public List<CuttingResponse> findMy(@RequestParam Long shiftId,
                                        @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return getCutting.findByOperatorAndShift(principal.userId(), shiftId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/available")
    public AvailableCuttingResponse findAvailable(@RequestParam String containerCode) {
        AvailableCuttingResult result = getCutting.findAvailableByContainerCode(containerCode);
        return new AvailableCuttingResponse(
                result.cuttingRecordId(),
                result.containerCode(),
                result.profileCode(),
                result.initialQuantity(),
                result.goodQuantity(),
                result.scrapQuantity(),
                result.cutAt()
        );
    }

    @GetMapping("/{id}")
    public CuttingResponse findById(@PathVariable Long id) {
        return toResponse(getCutting.findByIdWithCodes(id));
    }

    private CuttingResponse toResponse(CuttingResult result) {
        return new CuttingResponse(
                result.id(),
                result.inventoryItemId(),
                result.machineId(),
                result.operatorId(),
                result.shiftId(),
                result.initialQuantity(),
                result.goodQuantity(),
                result.scrapQuantity(),
                result.cutAt(),
                result.containerCode(),
                result.profileCode(),
                result.machineName()
        );
    }
}