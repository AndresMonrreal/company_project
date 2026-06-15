package com.example.company.molding.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MoldingOutputSpringRepository extends JpaRepository<MoldingOutputJpaEntity, Long> {

    @Query("SELECT m FROM MoldingOutputJpaEntity m WHERE m.operatorId = :operatorId AND m.sentAt BETWEEN :start AND :end ORDER BY m.sentAt ASC")
    List<MoldingOutputJpaEntity> findByOperatorAndWindow(
            @Param("operatorId") Long operatorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
