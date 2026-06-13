package com.example.company.machines.adapter.in.web;

import com.example.company.machines.adapter.in.web.dto.MachineCreateRequest;
import com.example.company.machines.adapter.in.web.dto.MachineResponse;
import com.example.company.machines.adapter.in.web.dto.MachineUpdateRequest;
import com.example.company.machines.domain.port.in.CreateMachineCommand;
import com.example.company.machines.domain.port.in.MachineResult;
import com.example.company.machines.domain.port.in.UpdateMachineCommand;
import org.springframework.stereotype.Component;

@Component
public class MachineWebMapper {

    CreateMachineCommand toCommand(MachineCreateRequest request) {
        return new CreateMachineCommand(request.name());
    }

    UpdateMachineCommand toCommand(MachineUpdateRequest request) {
        return new UpdateMachineCommand(request.name());
    }

    MachineResponse toResponse(MachineResult result) {
        return new MachineResponse(
                result.id(),
                result.name(),
                result.active()
        );
    }
}
