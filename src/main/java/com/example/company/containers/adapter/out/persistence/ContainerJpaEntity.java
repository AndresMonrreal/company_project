package com.example.company.containers.adapter.out.persistence;

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
@Table(name = "containers")
public class ContainerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "container_type_id", nullable = false)
    private Long containerTypeId;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected ContainerJpaEntity() {
    }

    ContainerJpaEntity(Long containerTypeId, String code, boolean active) {
        this.containerTypeId = containerTypeId;
        this.code = code;
        this.active = active;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    void updateFromDomain(Long containerTypeId, String code, boolean active) {
        this.containerTypeId = containerTypeId;
        this.code = code;
        this.active = active;
    }

    Long getId() {
        return id;
    }

    Long getContainerTypeId() {
        return containerTypeId;
    }

    String getCode() {
        return code;
    }

    boolean isActive() {
        return active;
    }
}
