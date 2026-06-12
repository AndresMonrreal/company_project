package com.example.company.shifts.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;

import com.example.company.shifts.domain.exception.DuplicateShiftNameException;
import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.in.ShiftResult;
import com.example.company.shifts.domain.port.in.UpdateShiftCommand;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateShiftServiceTest {

    @Mock
    private ShiftRepositoryPort shiftRepository;

    @InjectMocks
    private UpdateShiftService service;

    @Test
    void updatesShiftWhenNameIsUnique() {
        when(shiftRepository.findActiveById(1L)).thenReturn(Optional.of(Shift.restore(
                1L, "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0), true
        )));
        when(shiftRepository.existsByNameAndIdNot("Second Shift", 1L)).thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenReturn(Shift.restore(
                1L, "Second Shift", LocalTime.of(14, 0), LocalTime.of(22, 0), true
        ));

        ShiftResult result = service.update(1L, new UpdateShiftCommand(
                "Second Shift", LocalTime.of(14, 0), LocalTime.of(22, 0)
        ));

        assertThat(result.name()).isEqualTo("Second Shift");
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void rejectsMissingShift() {
        when(shiftRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateShiftCommand(
                "Second Shift", LocalTime.of(14, 0), LocalTime.of(22, 0)
        )))
                .isInstanceOf(ShiftNotFoundException.class)
                .hasMessage("Shift not found: 99");
    }

    @Test
    void rejectsDuplicateNameForAnotherShift() {
        when(shiftRepository.findActiveById(1L)).thenReturn(Optional.of(Shift.restore(
                1L, "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0), true
        )));
        when(shiftRepository.existsByNameAndIdNot("Second Shift", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateShiftCommand(
                "Second Shift", LocalTime.of(14, 0), LocalTime.of(22, 0)
        )))
                .isInstanceOf(DuplicateShiftNameException.class)
                .hasMessage("Shift name already exists: Second Shift");
    }
}
