package com.example.company.machines.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.company.machines.domain.exception.MachineNotFoundException;
import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteMachineServiceTest {

    @Mock
    private MachineRepositoryPort machineRepository;

    @InjectMocks
    private DeleteMachineService service;

    @Test
    void softDeletesMachine() {
        when(machineRepository.findActiveById(1L)).thenReturn(Optional.of(Machine.restore(1L, "CUT-01", true)));
        when(machineRepository.save(any(Machine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.delete(1L);

        ArgumentCaptor<Machine> captor = ArgumentCaptor.forClass(Machine.class);
        verify(machineRepository).save(captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void rejectsMissingMachine() {
        when(machineRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(MachineNotFoundException.class)
                .hasMessage("Machine not found: 99");
    }
}
