package com.example.company.machines.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.company.machines.domain.exception.DuplicateMachineNameException;
import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.in.CreateMachineCommand;
import com.example.company.machines.domain.port.in.MachineResult;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateMachineServiceTest {

    @Mock
    private MachineRepositoryPort machineRepository;

    @InjectMocks
    private CreateMachineService service;

    @Test
    void createsMachineWhenNameIsUnique() {
        when(machineRepository.existsByName("CUT-01")).thenReturn(false);
        when(machineRepository.save(any(Machine.class))).thenReturn(Machine.restore(1L, "CUT-01", true));

        MachineResult result = service.create(new CreateMachineCommand("CUT-01"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("CUT-01");
        assertThat(result.active()).isTrue();
        verify(machineRepository).save(any(Machine.class));
    }

    @Test
    void rejectsDuplicateName() {
        when(machineRepository.existsByName("CUT-01")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateMachineCommand("CUT-01")))
                .isInstanceOf(DuplicateMachineNameException.class)
                .hasMessage("Machine name already exists: CUT-01");
    }
}
