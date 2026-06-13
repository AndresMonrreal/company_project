package com.example.company.shifts.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.in.ShiftResult;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetShiftServiceTest {

    @Mock
    private ShiftRepositoryPort shiftRepository;

    @InjectMocks
    private GetShiftService service;

    @Test
    void listsActiveShifts() {
        when(shiftRepository.findAllActiveOrderByNameAsc()).thenReturn(List.of(
                Shift.restore(1L, "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0), true),
                Shift.restore(2L, "Second Shift", LocalTime.of(14, 0), LocalTime.of(22, 0), true)
        ));

        List<ShiftResult> results = service.findAllActive();

        assertThat(results).extracting(ShiftResult::name).containsExactly("First Shift", "Second Shift");
    }

    @Test
    void findsShiftById() {
        when(shiftRepository.findActiveById(1L)).thenReturn(Optional.of(Shift.restore(
                1L, "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0), true
        )));

        ShiftResult result = service.findById(1L);

        assertThat(result.name()).isEqualTo("First Shift");
    }

    @Test
    void rejectsMissingShift() {
        when(shiftRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ShiftNotFoundException.class)
                .hasMessage("Shift not found: 99");
    }
}
