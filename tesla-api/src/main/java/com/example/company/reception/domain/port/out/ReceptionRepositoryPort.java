package com.example.company.reception.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.reception.domain.model.Reception;
import com.example.company.reception.domain.port.in.ReceptionResult;

public interface ReceptionRepositoryPort {

    ReceptionResult save(Reception reception);

    List<ReceptionResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end);

    Optional<ReceptionResult> findById(Long id);
}
