package com.example.company.reception.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.company.reception.domain.exception.ReceptionNotFoundException;
import com.example.company.reception.domain.port.in.GetMyReceptionsUseCase;
import com.example.company.reception.domain.port.in.ReceptionResult;
import com.example.company.reception.domain.port.out.ReceptionRepositoryPort;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMyReceptionsService implements GetMyReceptionsUseCase {

    private final ReceptionRepositoryPort receptionRepository;
    private final ShiftRepositoryPort shiftRepository;

    public GetMyReceptionsService(ReceptionRepositoryPort receptionRepository,
                                  ShiftRepositoryPort shiftRepository) {
        this.receptionRepository = receptionRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        Shift shift = shiftRepository.findActiveById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));

        LocalDateTime windowStart = LocalDate.now().atTime(shift.startTime());

        LocalDateTime windowEnd;
        if (shift.endTime().isAfter(shift.startTime())) {
            windowEnd = LocalDate.now().atTime(shift.endTime());
        } else {
            windowEnd = LocalDate.now().plusDays(1).atTime(shift.endTime());
        }

        return receptionRepository.findByOperatorAndWindow(operatorId, windowStart, windowEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionResult findById(Long id) {
        return receptionRepository.findById(id)
                .orElseThrow(() -> new ReceptionNotFoundException(id));
    }
}
