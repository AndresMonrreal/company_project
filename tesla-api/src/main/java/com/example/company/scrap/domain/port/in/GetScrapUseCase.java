package com.example.company.scrap.domain.port.in;

import java.util.List;

public interface GetScrapUseCase {

    ScrapResult findById(Long id);

    List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId);
}