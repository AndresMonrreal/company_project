package com.example.company.shifts.application.usecase;

import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.in.DeleteShiftUseCase;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
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

        shift.deactivate();
        shiftRepository.save(shift);
    }
}
