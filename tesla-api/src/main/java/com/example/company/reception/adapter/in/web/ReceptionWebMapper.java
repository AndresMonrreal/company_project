package com.example.company.reception.adapter.in.web;

import com.example.company.reception.adapter.in.web.dto.RegisterReceptionRequest;
import com.example.company.reception.adapter.in.web.dto.ReceptionResponse;
import com.example.company.reception.domain.port.in.RegisterReceptionCommand;
import com.example.company.reception.domain.port.in.ReceptionResult;
import org.springframework.stereotype.Component;

@Component
class ReceptionWebMapper {

    RegisterReceptionCommand toCommand(RegisterReceptionRequest request, Long operatorId) {
        return new RegisterReceptionCommand(
                request.containerId(),
                request.profileId(),
                operatorId,
                request.lot(),
                request.receivedQuantity()
        );
    }

    ReceptionResponse toResponse(ReceptionResult result) {
        return new ReceptionResponse(
                result.id(),
                result.containerCode(),
                result.profileCode(),
                result.lot(),
                result.receivedQuantity(),
                result.status(),
                result.receivedAt()
        );
    }
}
