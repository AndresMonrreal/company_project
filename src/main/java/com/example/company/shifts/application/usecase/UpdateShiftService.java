package com.example.company.shifts.application.usecase;

import com.example.company.shifts.application.mapper.ShiftResultMapper;
import com.example.company.shifts.domain.exception.DuplicateShiftNameException;
import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.in.ShiftResult;
import com.example.company.shifts.domain.port.in.UpdateShiftCommand;
import com.example.company.shifts.domain.port.in.UpdateShiftUseCase;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateShiftService implements UpdateShiftUseCase {

    private final ShiftRepositoryPort shiftRepository;

    public UpdateShiftService(ShiftRepositoryPort shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Override
    @Transactional
    public ShiftResult update(Long id, UpdateShiftCommand command) {
        Shift shift = shiftRepository.findActiveById(id)
                .orElseThrow(() -> new ShiftNotFoundException(id));

        if (shiftRepository.existsByNameAndIdNot(command.name(), id)) {
            throw new DuplicateShiftNameException(command.name());
        }

        shift.update(command.name(), command.startTime(), command.endTime());
        return ShiftResultMapper.toResult(shiftRepository.save(shift));
    }
}
