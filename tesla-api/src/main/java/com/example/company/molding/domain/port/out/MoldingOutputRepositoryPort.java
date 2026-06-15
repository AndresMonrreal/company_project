package com.example.company.molding.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.molding.domain.model.MoldingOutput;
import com.example.company.molding.domain.port.in.MoldingOutputResult;

public interface MoldingOutputRepositoryPort {

    MoldingOutput save(MoldingOutput output);

    List<MoldingOutputResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end);

    Optional<MoldingOutputResult> findById(Long id);
}
