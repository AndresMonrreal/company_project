package com.example.company.cutting.adapter.in.web;

import com.example.company.cutting.adapter.in.web.dto.CuttingResponse;
import com.example.company.cutting.adapter.in.web.dto.RegisterCutRequest;
import com.example.company.cutting.domain.port.in.CuttingResult;
import com.example.company.cutting.domain.port.in.RegisterCutCommand;
import org.springframework.stereotype.Component;

@Component
class CuttingWebMapper {

    RegisterCutCommand toCommand(RegisterCutRequest request, Long operatorId) {
        return new RegisterCutCommand(
                request.inventoryItemId(),
                request.machineId(),
                operatorId,
                request.shiftId(),
                request.initialQuantity(),
                request.goodQuantity(),
                request.scrapQuantity()
        );
    }

    CuttingResponse toResponse(CuttingResult result) {
        return new CuttingResponse(
                result.id(),
                result.containerCode(),
                result.profileCode(),
                result.machineCode(),
                result.initialQuantity(),
                result.goodQuantity(),
                result.scrapQuantity(),
                result.cutAt()
        );
    }
}
