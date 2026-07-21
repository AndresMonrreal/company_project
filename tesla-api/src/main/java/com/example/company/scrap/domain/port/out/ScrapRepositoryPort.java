package com.example.company.scrap.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.ScrapResult;

public interface ScrapRepositoryPort {

    ScrapRecord save(ScrapRecord record);

    Optional<ScrapRecord> findById(Long id);

    List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId);
}