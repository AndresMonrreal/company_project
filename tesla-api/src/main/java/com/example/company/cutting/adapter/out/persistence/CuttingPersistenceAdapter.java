package com.example.company.cutting.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.cutting.domain.model.CuttingRecord;
import com.example.company.cutting.domain.port.in.CuttingResult;
import com.example.company.cutting.domain.port.out.CuttingRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class CuttingPersistenceAdapter implements CuttingRepositoryPort {

    private final CuttingSpringRepository repo;
    private final CuttingPersistenceMapper mapper;

    public CuttingPersistenceAdapter(CuttingSpringRepository repo, CuttingPersistenceMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public CuttingResult save(CuttingRecord record) {
        CuttingRecordJpaEntity saved = repo.save(mapper.toNewEntity(record));

        Object[] codes = repo.findCodesById(saved.getId())
                .orElse(new Object[]{"", "", ""});

        String containerCode = (String) codes[0];
        String profileCode   = (String) codes[1];
        String machineCode   = (String) codes[2];

        return new CuttingResult(
                saved.getId(),
                containerCode,
                profileCode,
                machineCode,
                saved.getInitialQuantity(),
                saved.getGoodQuantity(),
                saved.getScrapQuantity(),
                saved.getCutAt()
        );
    }

    @Override
    public List<CuttingResult> findByOperatorAndWindow(Long operatorId,
                                                       LocalDateTime start,
                                                       LocalDateTime end) {
        return repo.findByOperatorAndWindowRaw(operatorId, start, end)
                .stream()
                .map(row -> new CuttingResult(
                        ((Number) row[0]).longValue(),
                        (String) row[5],
                        (String) row[6],
                        (String) row[7],
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).intValue(),
                        ((Number) row[3]).intValue(),
                        (LocalDateTime) row[4]
                ))
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    @Override
    public Optional<CuttingResult> findById(Long id) {
        return repo.findById(id).flatMap(entity ->
                repo.findCodesById(id).map(codes -> new CuttingResult(
                        entity.getId(),
                        (String) codes[0],
                        (String) codes[1],
                        (String) codes[2],
                        entity.getInitialQuantity(),
                        entity.getGoodQuantity(),
                        entity.getScrapQuantity(),
                        entity.getCutAt()
                ))
        );
    }
}
