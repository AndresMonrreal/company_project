package com.example.company.profiles.adapter.out.persistence;

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
@Table(name = "profiles")
public class ProfileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "type", length = 10)
    private String type;

    @Column(name = "profile_position", length = 10)
    private String position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected ProfileJpaEntity() {
    }

    ProfileJpaEntity(String code, String name, String description, boolean active,
                     String type, String position) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
        this.type = type;
        this.position = position;
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

    void updateFromDomain(String code, String name, String description, boolean active,
                         String type, String position) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
        this.type = type;
        this.position = position;
    }

    Long getId() { return id; }
    String getCode() { return code; }
    String getName() { return name; }
    String getDescription() { return description; }
    boolean isActive() { return active; }
    String getType() { return type; }
    String getPosition() { return position; }
}
