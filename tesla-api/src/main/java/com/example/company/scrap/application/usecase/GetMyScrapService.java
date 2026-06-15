package com.example.company.scrap.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.company.scrap.domain.exception.ScrapRecordNotFoundException;
import com.example.company.scrap.domain.port.in.GetMyScrapUseCase;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMyScrapService implements GetMyScrapUseCase {

    private final ScrapRepositoryPort scrapRepository;
    private final ShiftRepositoryPort shiftRepository;

    public GetMyScrapService(ScrapRepositoryPort scrapRepository, ShiftRepositoryPort shiftRepository) {
        this.scrapRepository = scrapRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        Shift shift = shiftRepository.findActiveById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));

        LocalDateTime windowStart = LocalDate.now().atTime(shift.startTime());

        LocalDateTime windowEnd;
        if (shift.endTime().isAfter(shift.startTime())) {
            windowEnd = LocalDate.now().atTime(shift.endTime());
        } else {
            windowEnd = LocalDate.now().plusDays(1).atTime(shift.endTime());
        }

        return scrapRepository.findByOperatorAndWindow(operatorId, windowStart, windowEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public ScrapResult findById(Long id) {
        return scrapRepository.findById(id)
                .orElseThrow(() -> new ScrapRecordNotFoundException(id));
    }
}
