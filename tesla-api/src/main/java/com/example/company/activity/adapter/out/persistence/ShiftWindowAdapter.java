package com.example.company.activity.adapter.out.persistence;

import com.example.company.activity.domain.port.out.ShiftWindowPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ShiftWindowAdapter implements ShiftWindowPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ShiftWindow> findWindowByShiftId(Long shiftId) {
        String sql = "SELECT s.start_time, s.end_time FROM shifts s WHERE s.id = :shiftId AND s.active = true";
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("shiftId", shiftId)
                .getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] row = rows.get(0);
        return Optional.of(new ShiftWindow(toLocalTime(row[0]), toLocalTime(row[1])));
    }

    private LocalTime toLocalTime(Object value) {
        if (value instanceof LocalTime lt) return lt;
        if (value instanceof java.sql.Time t) return t.toLocalTime();
        throw new IllegalStateException("Unexpected time type: " + value.getClass().getName());
    }
}