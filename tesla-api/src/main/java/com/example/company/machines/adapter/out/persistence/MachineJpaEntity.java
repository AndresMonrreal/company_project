package com.example.company.machines.adapter.out.persistence;

import java.time.LocalDate;
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
@Table(name = "machines")
public class MachineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "processes_type", length = 10)
    private String processesType;

    @Column(name = "cycle_time_seconds")
    private Integer cycleTimeSeconds;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(length = 500)
    private String observations;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected MachineJpaEntity() {
    }

    MachineJpaEntity(String name, boolean active, String code, String status,
                     String processesType, Integer cycleTimeSeconds,
                     LocalDate lastMaintenanceDate, String observations) {
        this.name = name;
        this.active = active;
        this.code = code;
        this.status = status;
        this.processesType = processesType;
        this.cycleTimeSeconds = cycleTimeSeconds;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.observations = observations;
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

    void updateFromDomain(String name, boolean active, String code, String status,
                          String processesType, Integer cycleTimeSeconds,
                          LocalDate lastMaintenanceDate, String observations) {
        this.name = name;
        this.active = active;
        this.code = code;
        this.status = status;
        this.processesType = processesType;
        this.cycleTimeSeconds = cycleTimeSeconds;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.observations = observations;
    }

    Long getId() { return id; }
    String getName() { return name; }
    boolean isActive() { return active; }
    String getCode() { return code; }
    String getStatus() { return status; }
    String getProcessesType() { return processesType; }
    Integer getCycleTimeSeconds() { return cycleTimeSeconds; }
    LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    String getObservations() { return observations; }
}
