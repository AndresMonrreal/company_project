package com.example.company.scrap.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScrapSpringRepository extends JpaRepository<ScrapRecordJpaEntity, Long> {

    @Query(value = """
            SELECT sr.id, s.name, sr.shift_id, p.code, sr.profile_id,
                   sr.operator_id, sr.quantity, sr.reason, sr.created_at
            FROM scrap_records sr
            JOIN shifts s ON sr.shift_id = s.id
            JOIN profiles p ON sr.profile_id = p.id
            WHERE sr.operator_id = :operatorId AND sr.shift_id = :shiftId
            """, nativeQuery = true)
    List<Object[]> findWithDetailsByOperatorAndShift(
            @Param("operatorId") Long operatorId,
            @Param("shiftId") Long shiftId);
}