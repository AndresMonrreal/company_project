package com.example.company.molding.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.company.molding.domain.exception.MoldingOutputNotFoundException;
import com.example.company.molding.domain.port.in.GetMyMoldingOutputsUseCase;
import com.example.company.molding.domain.port.in.MoldingOutputResult;
import com.example.company.molding.domain.port.out.MoldingOutputRepositoryPort;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMyMoldingOutputsService implements GetMyMoldingOutputsUseCase {

    private final MoldingOutputRepositoryPort moldingOutputRepository;
    private final ShiftRepositoryPort shiftRepository;

    public GetMyMoldingOutputsService(
            MoldingOutputRepositoryPort moldingOutputRepository,
            ShiftRepositoryPort shiftRepository
    ) {
        this.moldingOutputRepository = moldingOutputRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldingOutputResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        Shift shift = shiftRepository.findActiveById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));

        LocalDateTime windowStart = LocalDate.now().atTime(shift.startTime());

        LocalDateTime windowEnd;
        if (shift.endTime().isAfter(shift.startTime())) {
            windowEnd = LocalDate.now().atTime(shift.endTime());
        } else {
            windowEnd = LocalDate.now().plusDays(1).atTime(shift.endTime());
        }

        return moldingOutputRepository.findByOperatorAndWindow(operatorId, windowStart, windowEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public MoldingOutputResult findById(Long id) {
        return moldingOutputRepository.findById(id)
                .orElseThrow(() -> new MoldingOutputNotFoundException(id));
    }
}
