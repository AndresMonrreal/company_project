package com.example.company.cutting.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CuttingSpringRepository extends JpaRepository<CuttingRecordJpaEntity, Long> {

    @Query(value = """
            SELECT cr.id, cr.initial_quantity, cr.good_quantity, cr.scrap_quantity, cr.cut_at,
                   c.code AS container_code, p.code AS profile_code, m.name AS machine_code
            FROM cutting_records cr
            JOIN inventory_items ii ON ii.id = cr.inventory_item_id
            JOIN receptions r ON r.id = ii.reception_id
            JOIN containers c ON c.id = r.container_id
            JOIN profiles p ON p.id = r.profile_id
            JOIN machines m ON m.id = cr."machine_Id"
            WHERE cr.operator_id = :operatorId
            AND cr.cut_at BETWEEN :start AND :end
            ORDER BY cr.cut_at ASC
            """, nativeQuery = true)
    List<Object[]> findByOperatorAndWindowRaw(
            @Param("operatorId") Long operatorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = """
            SELECT c.code AS container_code, p.code AS profile_code, m.name AS machine_code
            FROM cutting_records cr
            JOIN inventory_items ii ON ii.id = cr.inventory_item_id
            JOIN receptions r ON r.id = ii.reception_id
            JOIN containers c ON c.id = r.container_id
            JOIN profiles p ON p.id = r.profile_id
            JOIN machines m ON m.id = cr."machine_Id"
            WHERE cr.id = :id
            """, nativeQuery = true)
    Optional<Object[]> findCodesById(@Param("id") Long id);
}
