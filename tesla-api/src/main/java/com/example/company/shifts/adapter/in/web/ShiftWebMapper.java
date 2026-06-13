package com.example.company.shifts.adapter.in.web;

import com.example.company.shifts.adapter.in.web.dto.ShiftCreateRequest;
import com.example.company.shifts.adapter.in.web.dto.ShiftResponse;
import com.example.company.shifts.adapter.in.web.dto.ShiftUpdateRequest;
import com.example.company.shifts.domain.port.in.CreateShiftCommand;
import com.example.company.shifts.domain.port.in.ShiftResult;
import com.example.company.shifts.domain.port.in.UpdateShiftCommand;
import org.springframework.stereotype.Component;

@Component
public class ShiftWebMapper {

    CreateShiftCommand toCommand(ShiftCreateRequest request) {
        return new CreateShiftCommand(request.name(), request.startTime(), request.endTime());
    }

    UpdateShiftCommand toCommand(ShiftUpdateRequest request) {
        return new UpdateShiftCommand(request.name(), request.startTime(), request.endTime());
    }

    ShiftResponse toResponse(ShiftResult result) {
        return new ShiftResponse(
                result.id(),
                result.name(),
                result.startTime(),
                result.endTime(),
                result.active()
        );
    }
}
