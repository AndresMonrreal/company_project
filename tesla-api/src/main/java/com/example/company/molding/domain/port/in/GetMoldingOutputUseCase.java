package com.example.company.molding.domain.port.in;

import java.util.List;

public interface GetMoldingOutputUseCase {

    MoldingOutputResult findById(Long id);

    List<MoldingOutputResult> findByOperatorAndShift(Long operatorId, Long shiftId);
}