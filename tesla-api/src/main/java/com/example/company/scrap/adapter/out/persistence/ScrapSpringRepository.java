package com.example.company.scrap.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ScrapSpringRepository extends JpaRepository<ScrapRecordJpaEntity, Long> {

    @Query(value = """
            SELECT sr.id, sr.cutting_record_id, sr.quantity, sr.reason, sr.created_at
            FROM scrap_records sr
            JOIN cutting_records cr ON cr.id = sr.cutting_record_id
            WHERE cr.operator_id = :operatorId
            AND sr.created_at BETWEEN :start AND :end
            ORDER BY sr.created_at ASC
            """, nativeQuery = true)
    List<Object[]> findByOperatorAndWindowRaw(
            @Param("operatorId") Long operatorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
