
package com.example.company.molding.adapter.out.persistence;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MoldingOutputSpringRepository extends JpaRepository<MoldingOutputJpaEntity, Long> {

    @Query(value = """
            SELECT * FROM molding_outputs
            WHERE operator_id = :operatorId
            AND CAST(sent_at AS TIME) BETWEEN :startTime AND :endTime
            """, nativeQuery = true)
    List<MoldingOutputJpaEntity> findByOperatorAndSameDayWindow(
            @Param("operatorId") Long operatorId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query(value = """
            SELECT * FROM molding_outputs
            WHERE operator_id = :operatorId
            AND (CAST(sent_at AS TIME) >= :startTime OR CAST(sent_at AS TIME) <= :endTime)
            """, nativeQuery = true)
    List<MoldingOutputJpaEntity> findByOperatorAndOvernightWindow(
            @Param("operatorId") Long operatorId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}