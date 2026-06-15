package com.example.company.cutting.domain.port.in;

import java.util.List;

public interface GetMyCuttingRecordsUseCase {

    List<CuttingResult> findByOperatorAndShift(Long operatorId, Long shiftId);

    CuttingResult findById(Long id);
}
