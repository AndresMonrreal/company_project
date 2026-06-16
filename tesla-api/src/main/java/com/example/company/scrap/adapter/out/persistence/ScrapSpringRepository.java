package com.example.company.scrap.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScrapSpringRepository extends JpaRepository<ScrapRecordJpaEntity, Long> {

    @Query(value = """
            SELECT s.* FROM scrap_records s
            JOIN cutting_records cr ON s.cutting_record_id = cr.id
            WHERE cr.operator_id = :operatorId AND cr.shift_id = :shiftId
            """, nativeQuery = true)
    List<ScrapRecordJpaEntity> findByOperatorAndShift(
            @Param("operatorId") Long operatorId,
            @Param("shiftId") Long shiftId);
}