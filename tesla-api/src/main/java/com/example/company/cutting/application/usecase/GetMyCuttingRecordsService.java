package com.example.company.cutting.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.company.cutting.domain.exception.CuttingRecordNotFoundException;
import com.example.company.cutting.domain.port.in.CuttingResult;
import com.example.company.cutting.domain.port.in.GetMyCuttingRecordsUseCase;
import com.example.company.cutting.domain.port.out.CuttingRepositoryPort;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMyCuttingRecordsService implements GetMyCuttingRecordsUseCase {

    private final CuttingRepositoryPort cuttingRepository;
    private final ShiftRepositoryPort shiftRepository;

    public GetMyCuttingRecordsService(CuttingRepositoryPort cuttingRepository,
                                      ShiftRepositoryPort shiftRepository) {
        this.cuttingRepository = cuttingRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuttingResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        Shift shift = shiftRepository.findActiveById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));

        LocalDateTime windowStart = LocalDate.now().atTime(shift.startTime());

        LocalDateTime windowEnd;
        if (shift.endTime().isAfter(shift.startTime())) {
            windowEnd = LocalDate.now().atTime(shift.endTime());
        } else {
            windowEnd = LocalDate.now().plusDays(1).atTime(shift.endTime());
        }

        return cuttingRepository.findByOperatorAndWindow(operatorId, windowStart, windowEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public CuttingResult findById(Long id) {
        return cuttingRepository.findById(id)
                .orElseThrow(() -> new CuttingRecordNotFoundException(id));
    }
}
