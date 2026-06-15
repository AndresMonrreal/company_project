package com.example.company.activity.adapter.out.persistence;

import com.example.company.activity.domain.model.ActivityItem;
import com.example.company.activity.domain.port.out.ActivityQueryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class ActivityPersistenceAdapter implements ActivityQueryPort {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ActivityItem> findReceptions(Long operatorId, LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT r.id, r.received_at, c.code, p.code, r.received_quantity, r.status
                FROM receptions r
                JOIN containers c ON c.id = r.container_id
                JOIN profiles p ON p.id = r.profile_id
                WHERE r.operator_id = :operatorId
                AND r.received_at BETWEEN :start AND :end
                ORDER BY r.received_at ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("operatorId", operatorId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return rows.stream().map(row -> {
            LocalDateTime ts = toLocalDateTime(row[1]);
            int qty = ((Number) row[4]).intValue();
            return new ActivityItem(((Number) row[0]).longValue(), ts.format(TIME_FMT), (String) row[2], (String) row[3],
                    "RECEPTION", qty + " pcs", (String) row[5], ts);
        }).toList();
    }

    @Override
    public List<ActivityItem> findCuttingRecords(Long operatorId, LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT cr.id, cr.cut_at, c.code, p.code, cr.initial_quantity, cr.good_quantity, cr.scrap_quantity
                FROM cutting_records cr
                JOIN inventory_items ii ON ii.id = cr.inventory_item_id
                JOIN receptions r ON r.id = ii.reception_id
                JOIN containers c ON c.id = r.container_id
                JOIN profiles p ON p.id = r.profile_id
                WHERE cr.operator_id = :operatorId
                AND cr.cut_at BETWEEN :start AND :end
                ORDER BY cr.cut_at ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("operatorId", operatorId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return rows.stream().map(row -> {
            LocalDateTime ts = toLocalDateTime(row[1]);
            int initial = ((Number) row[4]).intValue();
            int good = ((Number) row[5]).intValue();
            int scrap = ((Number) row[6]).intValue();
            String quantities = initial + " → " + good + " good · " + scrap + " scrap";
            return new ActivityItem(((Number) row[0]).longValue(), ts.format(TIME_FMT), (String) row[2], (String) row[3],
                    "CUT", quantities, "CUT", ts);
        }).toList();
    }

    @Override
    public List<ActivityItem> findScrapRecords(Long operatorId, LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT sr.id, sr.created_at, c.code, p.code, sr.quantity
                FROM scrap_records sr
                JOIN cutting_records cr ON cr.id = sr.cutting_record_id
                JOIN inventory_items ii ON ii.id = cr.inventory_item_id
                JOIN receptions r ON r.id = ii.reception_id
                JOIN containers c ON c.id = r.container_id
                JOIN profiles p ON p.id = r.profile_id
                WHERE cr.operator_id = :operatorId
                AND sr.created_at BETWEEN :start AND :end
                ORDER BY sr.created_at ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("operatorId", operatorId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return rows.stream().map(row -> {
            LocalDateTime ts = toLocalDateTime(row[1]);
            int qty = ((Number) row[4]).intValue();
            return new ActivityItem(((Number) row[0]).longValue(), ts.format(TIME_FMT), (String) row[2], (String) row[3],
                    "SCRAP", qty + " pcs", null, ts);
        }).toList();
    }

    @Override
    public List<ActivityItem> findMoldingOutputs(Long operatorId, LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT mo.id, mo.sent_at, c.code, p.code, mo.quantity_sent
                FROM molding_outputs mo
                JOIN cutting_records cr ON cr.id = mo.cutting_record_id
                JOIN inventory_items ii ON ii.id = cr.inventory_item_id
                JOIN receptions r ON r.id = ii.reception_id
                JOIN containers c ON c.id = r.container_id
                JOIN profiles p ON p.id = r.profile_id
                WHERE mo.operator_id = :operatorId
                AND mo.sent_at BETWEEN :start AND :end
                ORDER BY mo.sent_at ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("operatorId", operatorId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return rows.stream().map(row -> {
            LocalDateTime ts = toLocalDateTime(row[1]);
            int qty = ((Number) row[4]).intValue();
            return new ActivityItem(((Number) row[0]).longValue(), ts.format(TIME_FMT), (String) row[2], (String) row[3],
                    "MOLDING_OUTPUT", qty + " pcs", null, ts);
        }).toList();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        throw new IllegalStateException("Unexpected timestamp type: " + value.getClass().getName());
    }
}
