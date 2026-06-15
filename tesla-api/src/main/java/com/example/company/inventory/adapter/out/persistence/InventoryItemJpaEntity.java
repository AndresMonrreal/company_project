package com.example.company.inventory.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
class InventoryItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reception_id", nullable = false, unique = true)
    private Long receptionId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected InventoryItemJpaEntity() {
    }

    InventoryItemJpaEntity(Long receptionId, int availableQuantity, String status, Long version, LocalDateTime updatedAt) {
        this.receptionId = receptionId;
        this.availableQuantity = availableQuantity;
        this.status = status;
        this.version = version;
        this.updatedAt = updatedAt;
    }

    void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    void setStatus(String status) {
        this.status = status;
    }

    void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    Long getId() {
        return id;
    }

    Long getReceptionId() {
        return receptionId;
    }

    int getAvailableQuantity() {
        return availableQuantity;
    }

    String getStatus() {
        return status;
    }

    Long getVersion() {
        return version;
    }

    LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
