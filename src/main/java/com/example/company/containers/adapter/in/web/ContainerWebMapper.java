package com.example.company.containers.adapter.in.web;

import com.example.company.containers.adapter.in.web.dto.ContainerCreateRequest;
import com.example.company.containers.adapter.in.web.dto.ContainerResponse;
import com.example.company.containers.adapter.in.web.dto.ContainerUpdateRequest;
import com.example.company.containers.domain.port.in.ContainerResult;
import com.example.company.containers.domain.port.in.CreateContainerCommand;
import com.example.company.containers.domain.port.in.UpdateContainerCommand;
import org.springframework.stereotype.Component;

@Component
public class ContainerWebMapper {

    CreateContainerCommand toCommand(ContainerCreateRequest request) {
        return new CreateContainerCommand(request.containerTypeId(), request.code());
    }

    UpdateContainerCommand toCommand(ContainerUpdateRequest request) {
        return new UpdateContainerCommand(request.containerTypeId(), request.code());
    }

    ContainerResponse toResponse(ContainerResult result) {
        return new ContainerResponse(
                result.id(),
                result.containerTypeId(),
                result.code(),
                result.active()
        );
    }
}
