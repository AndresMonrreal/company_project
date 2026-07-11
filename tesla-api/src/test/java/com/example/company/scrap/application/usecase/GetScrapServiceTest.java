package com.example.company.scrap.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import com.example.company.scrap.domain.exception.ScrapNotFoundException;
import com.example.company.scrap.domain.model.ScrapRecord;
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
class GetScrapServiceTest {

    @Mock
    private ScrapRepositoryPort scrapRepository;

    @Mock
    private ShiftRepositoryPort shiftRepository;

    @Mock
    private ProfileRepositoryPort profileRepository;

    @InjectMocks
    private GetScrapService service;

    @Test
    void findsScrapByIdResolvingShiftAndProfileNames() {
        ScrapRecord record = ScrapRecord.restore(1L, 10L, 20L, 30L, 5, "Defect",
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(scrapRepository.findById(1L)).thenReturn(Optional.of(record));
        when(shiftRepository.findById(10L)).thenReturn(Optional.of(
                Shift.restore(10L, "Matutino", LocalTime.of(6, 0), LocalTime.of(14, 0), true)
        ));
        when(profileRepository.findById(20L)).thenReturn(Optional.of(
                Profile.restore(20L, "EXTE0036", "Profile A", null, true, "HEADER", "FRONT")
        ));

        ScrapResult result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.shiftName()).isEqualTo("Matutino");
        assertThat(result.profileCode()).isEqualTo("EXTE0036");
        assertThat(result.quantity()).isEqualTo(5);
    }

    @Test
    void findsScrapByIdWithNullNamesWhenShiftAndProfileNotFound() {
        ScrapRecord record = ScrapRecord.restore(1L, 10L, 20L, 30L, 5, null,
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(scrapRepository.findById(1L)).thenReturn(Optional.of(record));
        when(shiftRepository.findById(10L)).thenReturn(Optional.empty());
        when(profileRepository.findById(20L)).thenReturn(Optional.empty());

        ScrapResult result = service.findById(1L);

        assertThat(result.shiftName()).isNull();
        assertThat(result.profileCode()).isNull();
    }

    @Test
    void throwsWhenScrapNotFound() {
        when(scrapRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ScrapNotFoundException.class);
    }

    @Test
    void findsByOperatorAndShiftDelegatesDirectly() {
        List<ScrapResult> expected = List.of(
                new ScrapResult(1L, 10L, "Matutino", 20L, "EXTE0036", 30L, 5, "Defect",
                        LocalDateTime.of(2024, 1, 15, 10, 0))
        );
        when(scrapRepository.findByOperatorAndShift(30L, 10L)).thenReturn(expected);

        List<ScrapResult> results = service.findByOperatorAndShift(30L, 10L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).shiftName()).isEqualTo("Matutino");
    }
}