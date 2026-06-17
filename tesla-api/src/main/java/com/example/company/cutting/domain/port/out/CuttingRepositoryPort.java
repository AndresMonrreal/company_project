package com.example.company.cutting.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.cutting.domain.model.AvailableCuttingResult;
import com.example.company.cutting.domain.model.CuttingRecord;
import com.example.company.cutting.domain.port.in.CuttingResult;

public interface CuttingRepositoryPort {

    CuttingRecord save(CuttingRecord record);

    Optional<CuttingRecord> findById(Long id);

    List<CuttingRecord> findByOperatorAndShift(Long operatorId, Long shiftId);

    Optional<AvailableCuttingResult> findAvailableByContainerCode(String containerCode);

    Optional<CuttingResult> findByIdWithCodes(Long id);
}