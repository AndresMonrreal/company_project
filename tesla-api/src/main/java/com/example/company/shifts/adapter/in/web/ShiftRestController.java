package com.example.company.shifts.adapter.in.web;

import java.util.List;

import com.example.company.shifts.adapter.in.web.dto.ShiftCreateRequest;
import com.example.company.shifts.adapter.in.web.dto.ShiftResponse;
import com.example.company.shifts.adapter.in.web.dto.ShiftUpdateRequest;
import com.example.company.shifts.domain.port.in.CreateShiftUseCase;
import com.example.company.shifts.domain.port.in.DeleteShiftUseCase;
import com.example.company.shifts.domain.port.in.GetShiftUseCase;
import com.example.company.shifts.domain.port.in.UpdateShiftUseCase;
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
@RequestMapping("/api/shifts")
public class ShiftRestController {

    private final CreateShiftUseCase createShift;
    private final GetShiftUseCase getShift;
    private final UpdateShiftUseCase updateShift;
    private final DeleteShiftUseCase deleteShift;
    private final ShiftWebMapper mapper;

    public ShiftRestController(
            CreateShiftUseCase createShift,
            GetShiftUseCase getShift,
            UpdateShiftUseCase updateShift,
            DeleteShiftUseCase deleteShift,
            ShiftWebMapper mapper
    ) {
        this.createShift = createShift;
        this.getShift = getShift;
        this.updateShift = updateShift;
        this.deleteShift = deleteShift;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ShiftResponse> findAllActive() {
        return getShift.findAllActive()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ShiftResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getShift.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponse create(@Valid @RequestBody ShiftCreateRequest request) {
        return mapper.toResponse(createShift.create(mapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    public ShiftResponse update(@PathVariable Long id, @Valid @RequestBody ShiftUpdateRequest request) {
        return mapper.toResponse(updateShift.update(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteShift.delete(id);
    }
}
