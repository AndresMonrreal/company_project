package com.example.company.cutting.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.cutting.domain.model.CuttingRecord;
import com.example.company.cutting.domain.port.in.CuttingResult;

public interface CuttingRepositoryPort {

    CuttingResult save(CuttingRecord record);

    List<CuttingResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end);

    boolean existsById(Long id);

    Optional<CuttingResult> findById(Long id);
}
