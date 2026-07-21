package com.example.company.shifts.application.usecase;

import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.in.DeleteShiftUseCase;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import com.example.company.shared.domain.exception.DomainException;
import com.example.company.shared.domain.exception.DomainErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteShiftService implements DeleteShiftUseCase {

    private final ShiftRepositoryPort shiftRepository;

    public DeleteShiftService(ShiftRepositoryPort shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Shift shift = shiftRepository.findActiveById(id)
                .orElseThrow(() -> new ShiftNotFoundException(id));

        if (shiftRepository.hasActiveCuttingRecords(id)) {
            throw new DomainException(DomainErrorType.CONFLICT, "shift.has-cutting-records", "Cannot delete shift because it has associated cutting records.") {};
        }

        shift.deactivate();
        shiftRepository.save(shift);
    }
}
