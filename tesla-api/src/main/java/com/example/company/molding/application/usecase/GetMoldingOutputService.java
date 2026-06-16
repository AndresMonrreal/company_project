package com.example.company.molding.application.usecase;

import java.util.List;

import com.example.company.molding.domain.exception.MoldingOutputNotFoundException;
import com.example.company.molding.domain.model.MoldingOutput;
import com.example.company.molding.domain.port.in.GetMoldingOutputUseCase;
import com.example.company.molding.domain.port.in.MoldingOutputResult;
import com.example.company.molding.domain.port.out.MoldingOutputRepositoryPort;
import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetMoldingOutputService implements GetMoldingOutputUseCase {

    private final MoldingOutputRepositoryPort moldingRepository;
    private final ShiftRepositoryPort shiftRepository;

    public GetMoldingOutputService(MoldingOutputRepositoryPort moldingRepository,
                                   ShiftRepositoryPort shiftRepository) {
        this.moldingRepository = moldingRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public MoldingOutputResult findById(Long id) {
        return moldingRepository.findById(id)
                .map(this::toResult)
                .orElseThrow(() -> new MoldingOutputNotFoundException(id));
    }

    @Override
    public List<MoldingOutputResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        Shift shift = shiftRepository.findActiveById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException(shiftId));
        boolean overnight = !shift.endTime().isAfter(shift.startTime());
        return moldingRepository
                .findByOperatorAndTimeWindow(operatorId, shift.startTime(), shift.endTime(), overnight)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private MoldingOutputResult toResult(MoldingOutput output) {
        return new MoldingOutputResult(
                output.id(),
                output.cuttingRecordId(),
                output.quantitySent(),
                output.operatorId(),
                output.sentAt()
        );
    }
}