package com.example.company.roles.adapter.in.web;

import java.util.List;

import com.example.company.roles.adapter.in.web.dto.RoleCreateRequest;
import com.example.company.roles.adapter.in.web.dto.RoleResponse;
import com.example.company.roles.adapter.in.web.dto.RoleUpdateRequest;
import com.example.company.roles.domain.port.in.CreateRoleUseCase;
import com.example.company.roles.domain.port.in.DeleteRoleUseCase;
import com.example.company.roles.domain.port.in.GetRoleUseCase;
import com.example.company.roles.domain.port.in.UpdateRoleUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleRestController {

    private final CreateRoleUseCase createRole;
    private final GetRoleUseCase getRole;
    private final UpdateRoleUseCase updateRole;
    private final DeleteRoleUseCase deleteRole;
    private final RoleWebMapper mapper;

    public RoleRestController(
            CreateRoleUseCase createRole,
            GetRoleUseCase getRole,
            UpdateRoleUseCase updateRole,
            DeleteRoleUseCase deleteRole,
            RoleWebMapper mapper
    ) {
        this.createRole = createRole;
        this.getRole = getRole;
        this.updateRole = updateRole;
        this.deleteRole = deleteRole;
        this.mapper = mapper;
    }

    @GetMapping
    public List<RoleResponse> findAllActive() {
        return getRole.findAllActive()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public RoleResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getRole.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody RoleCreateRequest request) {
        return mapper.toResponse(createRole.create(mapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return mapper.toResponse(updateRole.update(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteRole.delete(id);
    }
}
