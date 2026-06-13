package com.example.company.container_types.adapter.in.web;

import com.example.company.container_types.adapter.in.web.dto.ContainerTypeCreateRequest;
import com.example.company.container_types.adapter.in.web.dto.ContainerTypeResponse;
import com.example.company.container_types.adapter.in.web.dto.ContainerTypeUpdateRequest;
import com.example.company.container_types.domain.port.in.ContainerTypeResult;
import com.example.company.container_types.domain.port.in.CreateContainerTypeCommand;
import com.example.company.container_types.domain.port.in.UpdateContainerTypeCommand;
import org.springframework.stereotype.Component;

@Component
public class ContainerTypeWebMapper {

    CreateContainerTypeCommand toCommand(ContainerTypeCreateRequest request) {
        return new CreateContainerTypeCommand(request.name());
    }

    UpdateContainerTypeCommand toCommand(ContainerTypeUpdateRequest request) {
        return new UpdateContainerTypeCommand(request.name());
    }

    ContainerTypeResponse toResponse(ContainerTypeResult result) {
        return new ContainerTypeResponse(
                result.id(),
                result.name(),
                result.active()
        );
    }
}
