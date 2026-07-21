package com.example.company.scrap.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import com.example.company.scrap.domain.exception.ScrapProfileInactiveException;
import com.example.company.scrap.domain.exception.ScrapShiftInactiveException;
import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.RegisterScrapCommand;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterScrapServiceTest {

    @Mock
    private ScrapRepositoryPort scrapRepository;

    @Mock
    private ShiftRepositoryPort shiftRepository;

    @Mock
    private ProfileRepositoryPort profileRepository;

    @InjectMocks
    private RegisterScrapService service;

    private static final Shift ACTIVE_SHIFT = Shift.restore(
            1L, "Matutino", LocalTime.of(6, 0), LocalTime.of(14, 0), true);
    private static final Profile ACTIVE_PROFILE = Profile.restore(
            2L, "EXTE0036", "Profile A", null, true, "HEADER", "FRONT");

    @Test
    void registersScrapWhenShiftAndProfileAreActive() {
        when(shiftRepository.findActiveById(1L)).thenReturn(Optional.of(ACTIVE_SHIFT));
        when(profileRepository.findActiveById(2L)).thenReturn(Optional.of(ACTIVE_PROFILE));
        when(scrapRepository.save(any(ScrapRecord.class))).thenReturn(
                ScrapRecord.restore(10L, 1L, 2L, 3L, 5, "Defect", LocalDateTime.of(2024, 1, 15, 10, 0))
        );

        ScrapResult result = service.register(new RegisterScrapCommand(1L, 2L, 3L, 5, "Defect"));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.shiftId()).isEqualTo(1L);
        assertThat(result.shiftName()).isEqualTo("Matutino");
        assertThat(result.profileId()).isEqualTo(2L);
        assertThat(result.profileCode()).isEqualTo("EXTE0036");
        assertThat(result.operatorId()).isEqualTo(3L);
        assertThat(result.quantity()).isEqualTo(5);
        assertThat(result.reason()).isEqualTo("Defect");
    }

    @Test
    void throwsWhenShiftIsInactive() {
        when(shiftRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(new RegisterScrapCommand(99L, 2L, 3L, 5, null)))
                .isInstanceOf(ScrapShiftInactiveException.class);
    }

    @Test
    void throwsWhenProfileIsInactive() {
        when(shiftRepository.findActiveById(1L)).thenReturn(Optional.of(ACTIVE_SHIFT));
        when(profileRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(new RegisterScrapCommand(1L, 99L, 3L, 5, null)))
                .isInstanceOf(ScrapProfileInactiveException.class);
    }
}