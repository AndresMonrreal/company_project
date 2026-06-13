package com.example.company.containers.adapter.in.web;

import java.util.List;

import com.example.company.containers.adapter.in.web.dto.ContainerCreateRequest;
import com.example.company.containers.adapter.in.web.dto.ContainerResponse;
import com.example.company.containers.adapter.in.web.dto.ContainerUpdateRequest;
import com.example.company.containers.domain.port.in.CreateContainerUseCase;
import com.example.company.containers.domain.port.in.DeleteContainerUseCase;
import com.example.company.containers.domain.port.in.GetContainerUseCase;
import com.example.company.containers.domain.port.in.UpdateContainerUseCase;
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
@RequestMapping("/api/containers")
public class ContainerRestController {

    private final CreateContainerUseCase createContainer;
    private final GetContainerUseCase getContainer;
    private final UpdateContainerUseCase updateContainer;
    private final DeleteContainerUseCase deleteContainer;
    private final ContainerWebMapper mapper;

    public ContainerRestController(
            CreateContainerUseCase createContainer,
            GetContainerUseCase getContainer,
            UpdateContainerUseCase updateContainer,
            DeleteContainerUseCase deleteContainer,
            ContainerWebMapper mapper
    ) {
        this.createContainer = createContainer;
        this.getContainer = getContainer;
        this.updateContainer = updateContainer;
        this.deleteContainer = deleteContainer;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ContainerResponse> findAllActive() {
        return getContainer.findAllActive()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ContainerResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getContainer.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContainerResponse create(@Valid @RequestBody ContainerCreateRequest request) {
        return mapper.toResponse(createContainer.create(mapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    public ContainerResponse update(@PathVariable Long id, @Valid @RequestBody ContainerUpdateRequest request) {
        return mapper.toResponse(updateContainer.update(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteContainer.delete(id);
    }
}
