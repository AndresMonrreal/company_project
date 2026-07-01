package com.example.company.machines.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataMachineRepository extends JpaRepository<MachineJpaEntity, Long> {

    List<MachineJpaEntity> findByActiveTrueOrderByNameAsc();

    Optional<MachineJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT COUNT(c) > 0 FROM CuttingRecordJpaEntity c WHERE c.machineId = :machineId")
    boolean hasActiveCuttingRecords(@Param("machineId") Long machineId);
}
