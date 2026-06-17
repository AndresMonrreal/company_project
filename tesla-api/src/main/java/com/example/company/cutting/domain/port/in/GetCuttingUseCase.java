package com.example.company.cutting.domain.port.in;

import java.util.List;

import com.example.company.cutting.domain.model.AvailableCuttingResult;

public interface GetCuttingUseCase {

    CuttingResult findById(Long id);

    List<CuttingResult> findByOperatorAndShift(Long operatorId, Long shiftId);

    AvailableCuttingResult findAvailableByContainerCode(String containerCode);

    CuttingResult findByIdWithCodes(Long id);
}