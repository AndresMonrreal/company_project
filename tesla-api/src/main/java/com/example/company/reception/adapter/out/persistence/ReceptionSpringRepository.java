package com.example.company.reception.adapter.out.persistence;

import java.time.LocalTime;
import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceptionSpringRepository extends JpaRepository<ReceptionJpaEntity, Long> {

    @Query(value = """
            SELECT * FROM receptions
            WHERE operator_id = :operatorId
            AND CAST(received_at AS TIME) BETWEEN :startTime AND :endTime
            """, nativeQuery = true)
    List<ReceptionJpaEntity> findByOperatorAndSameDayWindow(
            @Param("operatorId") Long operatorId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query(value = """
            SELECT * FROM receptions
            WHERE operator_id = :operatorId
            AND (CAST(received_at AS TIME) >= :startTime OR CAST(received_at AS TIME) <= :endTime)
            """, nativeQuery = true)
    List<ReceptionJpaEntity> findByOperatorAndOvernightWindow(
            @Param("operatorId") Long operatorId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query(value = """
            SELECT r.id, r.lot, r.received_quantity, r.status, r.received_at,
                   c.code AS container_code, p.code AS profile_code,
                   r.container_id, r.profile_id, r.operator_id
            FROM receptions r
            JOIN containers c ON r.container_id = c.id
            JOIN profiles p ON r.profile_id = p.id
            WHERE r.id = :id
            """, nativeQuery = true)
    Optional<Object[]> findByIdWithCodes(@Param("id") Long id);
}