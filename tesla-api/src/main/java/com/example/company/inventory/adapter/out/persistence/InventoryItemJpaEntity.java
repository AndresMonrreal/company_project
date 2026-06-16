package com.example.company.inventory.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory_items")
public class InventoryItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reception_id", nullable = false, unique = true)
    private Long receptionId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected InventoryItemJpaEntity() {
    }

    InventoryItemJpaEntity(Long receptionId, int availableQuantity, String status) {
        this.receptionId = receptionId;
        this.availableQuantity = availableQuantity;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    Long getId() { return id; }
    Long getReceptionId() { return receptionId; }
    int getAvailableQuantity() { return availableQuantity; }
    String getStatus() { return status; }
}