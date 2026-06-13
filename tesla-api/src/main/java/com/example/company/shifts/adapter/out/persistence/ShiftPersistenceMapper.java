package com.example.company.shifts.adapter.out.persistence;

import com.example.company.shifts.domain.model.Shift;
import org.springframework.stereotype.Component;

@Component
public class ShiftPersistenceMapper {

    Shift toDomain(ShiftJpaEntity entity) {
        return Shift.restore(
                entity.getId(),
                entity.getName(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.isActive()
        );
    }

    ShiftJpaEntity toNewEntity(Shift shift) {
        return new ShiftJpaEntity(
                shift.name(),
                shift.startTime(),
                shift.endTime(),
                shift.active()
        );
    }
}
