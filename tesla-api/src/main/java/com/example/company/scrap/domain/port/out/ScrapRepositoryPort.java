package com.example.company.scrap.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.scrap.domain.model.ScrapRecord;

public interface ScrapRepositoryPort {

    ScrapRecord save(ScrapRecord record);

    Optional<ScrapRecord> findById(Long id);

    List<ScrapRecord> findByOperatorAndShift(Long operatorId, Long shiftId);
}