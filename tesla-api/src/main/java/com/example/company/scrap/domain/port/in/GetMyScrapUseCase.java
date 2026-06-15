package com.example.company.scrap.domain.port.in;

import java.util.List;

public interface GetMyScrapUseCase {

    List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId);

    ScrapResult findById(Long id);
}
