package com.example.company.reception.domain.port.in;

import java.util.List;

public interface GetReceptionUseCase {

    ReceptionResult findById(Long id);

    List<ReceptionResult> findByOperatorAndShift(Long operatorId, Long shiftId);
}