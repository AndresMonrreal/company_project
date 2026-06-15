package com.example.company.molding.domain.port.in;

import java.util.List;

public interface GetMyMoldingOutputsUseCase {

    List<MoldingOutputResult> findByOperatorAndShift(Long operatorId, Long shiftId);

    MoldingOutputResult findById(Long id);
}
