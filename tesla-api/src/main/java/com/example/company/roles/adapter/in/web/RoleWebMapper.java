package com.example.company.roles.adapter.in.web;

import com.example.company.roles.adapter.in.web.dto.RoleCreateRequest;
import com.example.company.roles.adapter.in.web.dto.RoleResponse;
import com.example.company.roles.adapter.in.web.dto.RoleUpdateRequest;
import com.example.company.roles.domain.port.in.CreateRoleCommand;
import com.example.company.roles.domain.port.in.RoleResult;
import com.example.company.roles.domain.port.in.UpdateRoleCommand;
import org.springframework.stereotype.Component;

@Component
public class RoleWebMapper {

    CreateRoleCommand toCommand(RoleCreateRequest request) {
        return new CreateRoleCommand(request.name(), request.description());
    }

    UpdateRoleCommand toCommand(RoleUpdateRequest request) {
        return new UpdateRoleCommand(request.name(), request.description());
    }

    RoleResponse toResponse(RoleResult result) {
        return new RoleResponse(
                result.id(),
                result.name(),
                result.description(),
                result.active()
        );
    }
}
