package com.example.company.activity.adapter.out.persistence;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

import com.example.company.activity.domain.model.ActivityAction;
import com.example.company.activity.domain.model.ActivityRawEntry;
import com.example.company.activity.domain.port.out.ActivityQueryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityPersistenceAdapter implements ActivityQueryPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<ActivityRawEntry> findReceptionsByOperatorAndTimeWindow(Long operatorId, LocalTime start, LocalTime end, boolean overnight) {
        String timeFilter = overnight
                ? "CAST(r.received_at AS TIME) >= :startTime OR CAST(r.received_at AS TIME) <= :endTime"
                : "CAST(r.received_at AS TIME) BETWEEN :startTime AND :endTime";
        String sql = "SELECT r.id, c.code, p.code, r.received_quantity, r.received_at " +
                "FROM receptions r " +
                "JOIN containers c ON r.container_id = c.id " +
                "JOIN profiles p ON r.profile_id = p.id " +
                "WHERE r.operator_id = :operatorId AND (" + timeFilter + ")";
        var query = em.createNativeQuery(sql);
        query.setParameter("operatorId", operatorId);
        query.setParameter("startTime", Time.valueOf(start));
        query.setParameter("endTime", Time.valueOf(end));
        return mapRows((List<Object[]>) query.getResultList(), this::mapReceptionRow);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ActivityRawEntry> findCuttingByOperatorAndShift(Long operatorId, Long shiftId) {
        String sql = "SELECT cr.id, c.code, p.code, cr.initial_quantity, cr.good_quantity, cr.scrap_quantity, cr.cut_at " +
                "FROM cutting_records cr " +
                "JOIN inventory_items ii ON cr.inventory_item_id = ii.id " +
                "JOIN receptions r ON ii.reception_id = r.id " +
                "JOIN containers c ON r.container_id = c.id " +
                "JOIN profiles p ON r.profile_id = p.id " +
                "WHERE cr.operator_id = :operatorId AND cr.shift_id = :shiftId";
        var query = em.createNativeQuery(sql);
        query.setParameter("operatorId", operatorId);
        query.setParameter("shiftId", shiftId);
        return mapRows((List<Object[]>) query.getResultList(), this::mapCuttingRow);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ActivityRawEntry> findScrapByOperatorAndShift(Long operatorId, Long shiftId) {
        String sql = "SELECT sr.id, c.code, p.code, sr.quantity, sr.created_at " +
                "FROM scrap_records sr " +
                "JOIN cutting_records cr ON sr.cutting_record_id = cr.id " +
                "JOIN inventory_items ii ON cr.inventory_item_id = ii.id " +
                "JOIN receptions r ON ii.reception_id = r.id " +
                "JOIN containers c ON r.container_id = c.id " +
                "JOIN profiles p ON r.profile_id = p.id " +
                "WHERE cr.operator_id = :operatorId AND cr.shift_id = :shiftId";
        var query = em.createNativeQuery(sql);
        query.setParameter("operatorId", operatorId);
        query.setParameter("shiftId", shiftId);
        return mapRows((List<Object[]>) query.getResultList(), this::mapScrapRow);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ActivityRawEntry> findMoldingByOperatorAndTimeWindow(Long operatorId, LocalTime start, LocalTime end, boolean overnight) {
        String timeFilter = overnight
                ? "CAST(mo.sent_at AS TIME) >= :startTime OR CAST(mo.sent_at AS TIME) <= :endTime"
                : "CAST(mo.sent_at AS TIME) BETWEEN :startTime AND :endTime";
        String sql = "SELECT mo.id, c.code, p.code, mo.quantity_sent, mo.sent_at " +
                "FROM molding_outputs mo " +
                "JOIN cutting_records cr ON mo.cutting_record_id = cr.id " +
                "JOIN inventory_items ii ON cr.inventory_item_id = ii.id " +
                "JOIN receptions r ON ii.reception_id = r.id " +
                "JOIN containers c ON r.container_id = c.id " +
                "JOIN profiles p ON r.profile_id = p.id " +
                "WHERE mo.operator_id = :operatorId AND (" + timeFilter + ")";
        var query = em.createNativeQuery(sql);
        query.setParameter("operatorId", operatorId);
        query.setParameter("startTime", Time.valueOf(start));
        query.setParameter("endTime", Time.valueOf(end));
        return mapRows((List<Object[]>) query.getResultList(), this::mapMoldingRow);
    }

    private List<ActivityRawEntry> mapRows(List<Object[]> rows, Function<Object[], ActivityRawEntry> mapper) {
        return rows.stream().map(mapper).toList();
    }

    private ActivityRawEntry mapReceptionRow(Object[] row) {
        return new ActivityRawEntry(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                ActivityAction.RECEPTION,
                toLocalDateTime(row[4]),
                ((Number) row[3]).intValue(),
                null,
                null
        );
    }

    private ActivityRawEntry mapCuttingRow(Object[] row) {
        return new ActivityRawEntry(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                ActivityAction.CUT,
                toLocalDateTime(row[6]),
                ((Number) row[3]).intValue(),
                ((Number) row[4]).intValue(),
                ((Number) row[5]).intValue()
        );
    }

    private ActivityRawEntry mapScrapRow(Object[] row) {
        return new ActivityRawEntry(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                ActivityAction.SCRAP,
                toLocalDateTime(row[4]),
                ((Number) row[3]).intValue(),
                null,
                null
        );
    }

    private ActivityRawEntry mapMoldingRow(Object[] row) {
        return new ActivityRawEntry(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                ActivityAction.MOLDING_OUTPUT,
                toLocalDateTime(row[4]),
                ((Number) row[3]).intValue(),
                null,
                null
        );
    }

    private LocalDateTime toLocalDateTime(Object tsObj) {
        if (tsObj instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return (LocalDateTime) tsObj;
    }
}