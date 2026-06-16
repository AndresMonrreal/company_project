package com.example.company.shifts.application.usecase;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.shifts.application.mapper.ShiftResultMapper;
import com.example.company.shifts.domain.exception.ShiftNotFoundException;
import com.example.company.shifts.domain.port.in.GetShiftUseCase;
import com.example.company.shifts.domain.port.in.ShiftResult;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetShiftService implements GetShiftUseCase {

    private final ShiftRepositoryPort shiftRepository;

    public GetShiftService(ShiftRepositoryPort shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Override
    public List<ShiftResult> findAllActive() {
        return shiftRepository.findAllActiveOrderByNameAsc()
                .stream()
                .map(ShiftResultMapper::toResult)
                .toList();
    }

    @Override
    public ShiftResult findById(Long id) {
        return shiftRepository.findActiveById(id)
                .map(ShiftResultMapper::toResult)
                .orElseThrow(() -> new ShiftNotFoundException(id));
    }

    @Override
    public Optional<ShiftResult> findCurrent() {
        return shiftRepository.findCurrentByTime(LocalTime.now())
                .map(ShiftResultMapper::toResult);
    }
}
