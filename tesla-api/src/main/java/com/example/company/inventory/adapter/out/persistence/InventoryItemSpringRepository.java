package com.example.company.inventory.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface InventoryItemSpringRepository extends JpaRepository<InventoryItemJpaEntity, Long> {
}
