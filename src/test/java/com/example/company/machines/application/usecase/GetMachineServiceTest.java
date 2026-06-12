package com.example.company.machines.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.company.machines.domain.exception.MachineNotFoundException;
import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.in.MachineResult;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMachineServiceTest {

    @Mock
    private MachineRepositoryPort machineRepository;

    @InjectMocks
    private GetMachineService service;

    @Test
    void listsActiveMachines() {
        when(machineRepository.findAllActiveOrderByNameAsc()).thenReturn(List.of(
                Machine.restore(1L, "CUT-01", true),
                Machine.restore(2L, "CUT-02", true)
        ));

        List<MachineResult> results = service.findAllActive();

        assertThat(results).extracting(MachineResult::name).containsExactly("CUT-01", "CUT-02");
    }

    @Test
    void findsMachineById() {
        when(machineRepository.findActiveById(1L)).thenReturn(Optional.of(Machine.restore(1L, "CUT-01", true)));

        MachineResult result = service.findById(1L);

        assertThat(result.name()).isEqualTo("CUT-01");
    }

    @Test
    void rejectsMissingMachine() {
        when(machineRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(MachineNotFoundException.class)
                .hasMessage("Machine not found: 99");
    }
}
