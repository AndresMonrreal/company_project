package com.example.company.shifts.adapter.out.persistence;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class ShiftPersistenceAdapter implements ShiftRepositoryPort {

    private final SpringDataShiftRepository shiftRepository;
    private final ShiftPersistenceMapper mapper;

    public ShiftPersistenceAdapter(
            SpringDataShiftRepository shiftRepository,
            ShiftPersistenceMapper mapper
    ) {
        this.shiftRepository = shiftRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Shift> findAllActiveOrderByNameAsc() {
        return shiftRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Shift> findActiveById(Long id) {
        return shiftRepository.findByIdAndActiveTrue(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return shiftRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return shiftRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public boolean hasActiveCuttingRecords(Long shiftId) {
        return shiftRepository.hasActiveCuttingRecords(shiftId);
    }

    @Override
    public Optional<Shift> findCurrentByTime(LocalTime now) {
        return shiftRepository.findCurrentByTime(now).stream().findFirst().map(mapper::toDomain);
    }

    @Override
    public Shift save(Shift shift) {
        ShiftJpaEntity entity = shift.id() == null
                ? mapper.toNewEntity(shift)
                : shiftRepository.findById(shift.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Shift entity disappeared during save: " + shift.id()
                        ));

        entity.updateFromDomain(shift.name(), shift.startTime(), shift.endTime(), shift.active());
        return mapper.toDomain(shiftRepository.save(entity));
    }
}
