package com.example.company.reception.domain.port.in;

import java.util.List;

public interface GetMyReceptionsUseCase {

    List<ReceptionResult> findByOperatorAndShift(Long operatorId, Long shiftId);

    ReceptionResult findById(Long id);
}
