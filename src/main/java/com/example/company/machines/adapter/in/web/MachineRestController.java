package com.example.company.machines.adapter.in.web;

import java.util.List;

import com.example.company.machines.adapter.in.web.dto.MachineCreateRequest;
import com.example.company.machines.adapter.in.web.dto.MachineResponse;
import com.example.company.machines.adapter.in.web.dto.MachineUpdateRequest;
import com.example.company.machines.domain.port.in.CreateMachineUseCase;
import com.example.company.machines.domain.port.in.DeleteMachineUseCase;
import com.example.company.machines.domain.port.in.GetMachineUseCase;
import com.example.company.machines.domain.port.in.UpdateMachineUseCase;
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
@RequestMapping("/api/machines")
public class MachineRestController {

    private final CreateMachineUseCase createMachine;
    private final GetMachineUseCase getMachine;
    private final UpdateMachineUseCase updateMachine;
    private final DeleteMachineUseCase deleteMachine;
    private final MachineWebMapper mapper;

    public MachineRestController(
            CreateMachineUseCase createMachine,
            GetMachineUseCase getMachine,
            UpdateMachineUseCase updateMachine,
            DeleteMachineUseCase deleteMachine,
            MachineWebMapper mapper
    ) {
        this.createMachine = createMachine;
        this.getMachine = getMachine;
        this.updateMachine = updateMachine;
        this.deleteMachine = deleteMachine;
        this.mapper = mapper;
    }

    @GetMapping
    public List<MachineResponse> findAllActive() {
        return getMachine.findAllActive()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public MachineResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getMachine.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MachineResponse create(@Valid @RequestBody MachineCreateRequest request) {
        return mapper.toResponse(createMachine.create(mapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    public MachineResponse update(@PathVariable Long id, @Valid @RequestBody MachineUpdateRequest request) {
        return mapper.toResponse(updateMachine.update(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteMachine.delete(id);
    }
}
