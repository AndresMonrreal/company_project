package com.example.company.inventory.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryItemSpringRepository extends JpaRepository<InventoryItemJpaEntity, Long> {

    @Query(value = """
            SELECT ii.id, c.code, p.code, r.lot, ii.available_quantity
            FROM inventory_items ii
            JOIN receptions r ON ii.reception_id = r.id
            JOIN containers c ON r.container_id = c.id
            JOIN profiles p ON r.profile_id = p.id
            WHERE ii.status = 'AVAILABLE' AND c.code = :containerCode
            LIMIT 1
            """, nativeQuery = true)
    Optional<Object[]> findAvailableByContainerCode(@Param("containerCode") String containerCode);

    @Modifying
    @Query(value = "UPDATE inventory_items SET status = 'CUT', updated_at = CURRENT_TIMESTAMP WHERE id = :id",
            nativeQuery = true)
    void markAsCut(@Param("id") Long id);
}