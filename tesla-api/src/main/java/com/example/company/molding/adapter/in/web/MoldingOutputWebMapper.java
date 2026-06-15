package com.example.company.molding.adapter.in.web;

import com.example.company.molding.adapter.in.web.dto.MoldingOutputResponse;
import com.example.company.molding.adapter.in.web.dto.RegisterMoldingOutputRequest;
import com.example.company.molding.domain.port.in.MoldingOutputResult;
import com.example.company.molding.domain.port.in.RegisterMoldingOutputCommand;
import org.springframework.stereotype.Component;

@Component
class MoldingOutputWebMapper {

    RegisterMoldingOutputCommand toCommand(RegisterMoldingOutputRequest request, Long operatorId) {
        return new RegisterMoldingOutputCommand(
                request.cuttingRecordId(),
                operatorId,
                request.quantitySent()
        );
    }

    MoldingOutputResponse toResponse(MoldingOutputResult result) {
        return new MoldingOutputResponse(
                result.id(),
                result.cuttingRecordId(),
                result.quantitySent(),
                result.sentAt()
        );
    }
}
