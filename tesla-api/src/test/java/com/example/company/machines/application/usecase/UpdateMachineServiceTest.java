package com.example.company.machines.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.machines.domain.exception.DuplicateMachineCodeException;
import com.example.company.machines.domain.exception.DuplicateMachineNameException;
import com.example.company.machines.domain.exception.MachineNotFoundException;
import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.in.MachineResult;
import com.example.company.machines.domain.port.in.UpdateMachineCommand;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateMachineServiceTest {

    @Mock
    private MachineRepositoryPort machineRepository;

    @InjectMocks
    private UpdateMachineService service;

    @Test
    void updatesMachineWhenNameIsUnique() {
        when(machineRepository.findActiveById(1L)).thenReturn(Optional.of(
                Machine.restore(1L, "CUT-01", true, null, "OPERATIONAL", null, null, null, null)
        ));
        when(machineRepository.existsByNameAndIdNot("CUT-02", 1L)).thenReturn(false);
        when(machineRepository.save(any(Machine.class))).thenReturn(
                Machine.restore(1L, "CUT-02", true, null, "OPERATIONAL", null, null, null, null)
        );

        MachineResult result = service.update(1L, new UpdateMachineCommand(
                "CUT-02", null, "OPERATIONAL", null, null, null, null
        ));

        assertThat(result.name()).isEqualTo("CUT-02");
        verify(machineRepository).save(any(Machine.class));
    }

    @Test
    void rejectsMissingMachine() {
        when(machineRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateMachineCommand(
                "CUT-02", null, "OPERATIONAL", null, null, null, null
        )))
                .isInstanceOf(MachineNotFoundException.class)
                .hasMessage("Machine not found: 99");
    }

    @Test
    void rejectsDuplicateNameForAnotherMachine() {
        when(machineRepository.findActiveById(1L)).thenReturn(Optional.of(
                Machine.restore(1L, "CUT-01", true, null, "OPERATIONAL", null, null, null, null)
        ));
        when(machineRepository.existsByNameAndIdNot("CUT-02", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateMachineCommand(
                "CUT-02", null, "OPERATIONAL", null, null, null, null
        )))
                .isInstanceOf(DuplicateMachineNameException.class)
                .hasMessage("Machine name already exists: CUT-02");
    }

    @Test
    void rejectsDuplicateCodeForAnotherMachine() {
        when(machineRepository.findActiveById(1L)).thenReturn(Optional.of(
                Machine.restore(1L, "CUT-01", true, "OLD", "OPERATIONAL", null, null, null, null)
        ));
        when(machineRepository.existsByNameAndIdNot("CUT-01", 1L)).thenReturn(false);
        when(machineRepository.existsByCodeAndIdNot("M002", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateMachineCommand(
                "CUT-01", "M002", "OPERATIONAL", null, null, null, null
        )))
                .isInstanceOf(DuplicateMachineCodeException.class)
                .hasMessage("Machine code already exists: M002");
    }
}