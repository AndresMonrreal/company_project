package com.example.company.cutting.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuttingSpringRepository extends JpaRepository<CuttingRecordJpaEntity, Long> {

    List<CuttingRecordJpaEntity> findByOperatorIdAndShiftId(Long operatorId, Long shiftId);

    @Query(value = """
            SELECT cr.id, c.code, p.code, cr.initial_quantity, cr.good_quantity, cr.scrap_quantity, cr.cut_at
            FROM cutting_records cr
            JOIN inventory_items ii ON cr.inventory_item_id = ii.id
            JOIN receptions r ON ii.reception_id = r.id
            JOIN containers c ON r.container_id = c.id
            JOIN profiles p ON r.profile_id = p.id
            WHERE r.lot = :lot
            ORDER BY cr.cut_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Object[]> findAvailableByLot(@Param("lot") String lot);

    @Query(value = """
            SELECT cr.id, cr.initial_quantity, cr.good_quantity, cr.scrap_quantity, cr.cut_at,
                   c.code AS container_code, p.code AS profile_code, m.name AS machine_name,
                   cr.inventory_item_id, cr.machine_id, cr.operator_id, cr.shift_id
            FROM cutting_records cr
            JOIN inventory_items ii ON cr.inventory_item_id = ii.id
            JOIN receptions r ON ii.reception_id = r.id
            JOIN containers c ON r.container_id = c.id
            JOIN profiles p ON r.profile_id = p.id
            JOIN machines m ON cr.machine_id = m.id
            WHERE cr.id = :id
            """, nativeQuery = true)
    Optional<Object[]> findByIdWithCodes(@Param("id") Long id);
}