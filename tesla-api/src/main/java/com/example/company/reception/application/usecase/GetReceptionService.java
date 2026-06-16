package com.example.company.reception.application.usecase;

import java.util.List;

import com.example.company.reception.domain.exception.ReceptionNotFoundException;
import com.example.company.reception.domain.model.Reception;
import com.example.company.reception.domain.port.in.GetReceptionUseCase;
import com.example.company.reception.domain.port.in.ReceptionResult;
import com.example.company.reception.domain.port.out.ReceptionRepositoryPort;
import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetReceptionService implements GetReceptionUseCase {

    private final ReceptionRepositoryPort receptionRepository;
    private final ShiftRepositoryPort shiftRepository;

    public GetReceptionService(ReceptionRepositoryPort receptionRepository,
                               ShiftRepositoryPort shiftRepository) {
        this.receptionRepository = receptionRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public ReceptionResult findById(Long id) {
        return receptionRepository.findById(id)
                .map(this::toResult)
                .orElseThrow(() -> new ReceptionNotFoundException(id));
    }

    @Override
    public List<ReceptionResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        Shift shift = shiftRepository.findActiveById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException(shiftId));
        boolean overnight = !shift.endTime().isAfter(shift.startTime());
        return receptionRepository
                .findByOperatorAndTimeWindow(operatorId, shift.startTime(), shift.endTime(), overnight)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private ReceptionResult toResult(Reception reception) {
        return new ReceptionResult(
                reception.id(),
                reception.containerId(),
                reception.profileId(),
                reception.operatorId(),
                reception.lot(),
                reception.receivedQuantity(),
                reception.status().name(),
                reception.receivedAt()
        );
    }
}