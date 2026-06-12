package com.example.company.shifts.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;

import com.example.company.shifts.domain.exception.DuplicateShiftNameException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.in.CreateShiftCommand;
import com.example.company.shifts.domain.port.in.ShiftResult;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateShiftServiceTest {

    @Mock
    private ShiftRepositoryPort shiftRepository;

    @InjectMocks
    private CreateShiftService service;

    @Test
    void createsShiftWhenNameIsUnique() {
        when(shiftRepository.existsByName("First Shift")).thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenReturn(Shift.restore(
                1L, "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0), true
        ));

        ShiftResult result = service.create(new CreateShiftCommand(
                "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0)
        ));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("First Shift");
        assertThat(result.active()).isTrue();
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void rejectsDuplicateName() {
        when(shiftRepository.existsByName("First Shift")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateShiftCommand(
                "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0)
        )))
                .isInstanceOf(DuplicateShiftNameException.class)
                .hasMessage("Shift name already exists: First Shift");
    }
}
