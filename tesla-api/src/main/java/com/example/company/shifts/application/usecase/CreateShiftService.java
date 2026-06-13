package com.example.company.shifts.application.usecase;

import com.example.company.shifts.application.mapper.ShiftResultMapper;
import com.example.company.shifts.domain.exception.DuplicateShiftNameException;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.in.CreateShiftCommand;
import com.example.company.shifts.domain.port.in.CreateShiftUseCase;
import com.example.company.shifts.domain.port.in.ShiftResult;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateShiftService implements CreateShiftUseCase {

    private final ShiftRepositoryPort shiftRepository;

    public CreateShiftService(ShiftRepositoryPort shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Override
    @Transactional
    public ShiftResult create(CreateShiftCommand command) {
        if (shiftRepository.existsByName(command.name())) {
            throw new DuplicateShiftNameException(command.name());
        }

        Shift shift = Shift.create(command.name(), command.startTime(), command.endTime());
        return ShiftResultMapper.toResult(shiftRepository.save(shift));
    }
}
