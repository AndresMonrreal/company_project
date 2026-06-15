package com.example.company.scrap.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.ScrapResult;

public interface ScrapRepositoryPort {

    ScrapRecord save(ScrapRecord record);

    List<ScrapResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end);

    Optional<ScrapResult> findById(Long id);
}
