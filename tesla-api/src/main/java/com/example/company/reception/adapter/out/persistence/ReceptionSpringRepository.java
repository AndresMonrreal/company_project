package com.example.company.reception.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ReceptionSpringRepository extends JpaRepository<ReceptionJpaEntity, Long> {

    @Query(value = """
            SELECT r.id, r.lot, r.received_quantity, r.status, r.received_at,
                   c.code AS container_code, p.code AS profile_code
            FROM receptions r
            JOIN containers c ON c.id = r.container_id
            JOIN profiles p ON p.id = r.profile_id
            WHERE r.operator_id = :operatorId
            AND r.received_at BETWEEN :start AND :end
            ORDER BY r.received_at ASC
            """, nativeQuery = true)
    List<Object[]> findByOperatorAndWindowRaw(
            @Param("operatorId") Long operatorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = """
            SELECT r.id, r.lot, r.received_quantity, r.status, r.received_at,
                   c.code AS container_code, p.code AS profile_code
            FROM receptions r
            JOIN containers c ON c.id = r.container_id
            JOIN profiles p ON p.id = r.profile_id
            WHERE r.id = :id
            """, nativeQuery = true)
    Optional<Object[]> findByIdWithCodes(@Param("id") Long id);

    @Query(value = "SELECT code FROM containers WHERE id = :id", nativeQuery = true)
    Optional<String> findContainerCodeById(@Param("id") Long id);

    @Query(value = "SELECT code FROM profiles WHERE id = :id", nativeQuery = true)
    Optional<String> findProfileCodeById(@Param("id") Long id);
}
