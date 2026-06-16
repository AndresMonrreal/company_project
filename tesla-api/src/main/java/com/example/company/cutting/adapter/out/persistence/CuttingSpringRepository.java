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
            WHERE c.code = :containerCode
            ORDER BY cr.cut_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Object[]> findAvailableByContainerCode(@Param("containerCode") String containerCode);
}