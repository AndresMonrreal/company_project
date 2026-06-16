package com.example.company.reception.domain.port.out;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.reception.domain.model.Reception;

public interface ReceptionRepositoryPort {

    Reception save(Reception reception);

    Optional<Reception> findById(Long id);

    List<Reception> findByOperatorAndTimeWindow(Long operatorId, LocalTime startTime, LocalTime endTime,
                                                boolean overnight);
}