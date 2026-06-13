package com.example.company.shifts.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;

import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteShiftServiceTest {

    @Mock
    private ShiftRepositoryPort shiftRepository;

    @InjectMocks
    private DeleteShiftService service;

    @Test
    void softDeletesShift() {
        when(shiftRepository.findActiveById(1L)).thenReturn(Optional.of(Shift.restore(
                1L, "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0), true
        )));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.delete(1L);

        ArgumentCaptor<Shift> captor = ArgumentCaptor.forClass(Shift.class);
        verify(shiftRepository).save(captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void rejectsMissingShift() {
        when(shiftRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ShiftNotFoundException.class)
                .hasMessage("Shift not found: 99");
    }
}
