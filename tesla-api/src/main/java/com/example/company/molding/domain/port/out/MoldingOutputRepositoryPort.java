package com.example.company.molding.domain.port.out;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.molding.domain.model.MoldingOutput;

public interface MoldingOutputRepositoryPort {

    MoldingOutput save(MoldingOutput output);

    Optional<MoldingOutput> findById(Long id);

    List<MoldingOutput> findByOperatorAndTimeWindow(Long operatorId, LocalTime startTime,
                                                    LocalTime endTime, boolean overnight);
}