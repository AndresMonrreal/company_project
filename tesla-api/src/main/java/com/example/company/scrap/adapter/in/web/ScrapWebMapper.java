package com.example.company.scrap.adapter.in.web;

import com.example.company.scrap.adapter.in.web.dto.RegisterScrapRequest;
import com.example.company.scrap.adapter.in.web.dto.ScrapResponse;
import com.example.company.scrap.domain.port.in.RegisterScrapCommand;
import com.example.company.scrap.domain.port.in.ScrapResult;
import org.springframework.stereotype.Component;

@Component
class ScrapWebMapper {

    RegisterScrapCommand toCommand(RegisterScrapRequest request, Long operatorId) {
        return new RegisterScrapCommand(
                request.cuttingRecordId(),
                operatorId,
                request.quantity(),
                request.reason()
        );
    }

    ScrapResponse toResponse(ScrapResult result) {
        return new ScrapResponse(
                result.id(),
                result.cuttingRecordId(),
                result.quantity(),
                result.reason(),
                result.createdAt()
        );
    }
}
