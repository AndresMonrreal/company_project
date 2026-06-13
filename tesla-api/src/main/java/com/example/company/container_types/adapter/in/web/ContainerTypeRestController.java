package com.example.company.container_types.adapter.in.web;

import java.util.List;

import com.example.company.container_types.adapter.in.web.dto.ContainerTypeCreateRequest;
import com.example.company.container_types.adapter.in.web.dto.ContainerTypeResponse;
import com.example.company.container_types.adapter.in.web.dto.ContainerTypeUpdateRequest;
import com.example.company.container_types.domain.port.in.CreateContainerTypeUseCase;
import com.example.company.container_types.domain.port.in.DeleteContainerTypeUseCase;
import com.example.company.container_types.domain.port.in.GetContainerTypeUseCase;
import com.example.company.container_types.domain.port.in.UpdateContainerTypeUseCase;
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
@RequestMapping("/api/container-types")
public class ContainerTypeRestController {

    private final CreateContainerTypeUseCase createContainerType;
    private final GetContainerTypeUseCase getContainerType;
    private final UpdateContainerTypeUseCase updateContainerType;
    private final DeleteContainerTypeUseCase deleteContainerType;
    private final ContainerTypeWebMapper mapper;

    public ContainerTypeRestController(
            CreateContainerTypeUseCase createContainerType,
            GetContainerTypeUseCase getContainerType,
            UpdateContainerTypeUseCase updateContainerType,
            DeleteContainerTypeUseCase deleteContainerType,
            ContainerTypeWebMapper mapper
    ) {
        this.createContainerType = createContainerType;
        this.getContainerType = getContainerType;
        this.updateContainerType = updateContainerType;
        this.deleteContainerType = deleteContainerType;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ContainerTypeResponse> findAllActive() {
        return getContainerType.findAllActive()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ContainerTypeResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getContainerType.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContainerTypeResponse create(@Valid @RequestBody ContainerTypeCreateRequest request) {
        return mapper.toResponse(createContainerType.create(mapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    public ContainerTypeResponse update(@PathVariable Long id, @Valid @RequestBody ContainerTypeUpdateRequest request) {
        return mapper.toResponse(updateContainerType.update(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteContainerType.delete(id);
    }
}
