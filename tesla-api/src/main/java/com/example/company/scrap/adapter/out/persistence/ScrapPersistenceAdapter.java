package com.example.company.scrap.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.cutting.domain.port.out.CuttingRepositoryPort;
import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.scrap.domain.port.out.CuttingRecordExistsPort;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class ScrapPersistenceAdapter implements ScrapRepositoryPort, CuttingRecordExistsPort {

    private final ScrapSpringRepository repo;
    private final ScrapPersistenceMapper mapper;
    private final CuttingRepositoryPort cuttingRepositoryPort;

    public ScrapPersistenceAdapter(
            ScrapSpringRepository repo,
            ScrapPersistenceMapper mapper,
            CuttingRepositoryPort cuttingRepositoryPort
    ) {
        this.repo = repo;
        this.mapper = mapper;
        this.cuttingRepositoryPort = cuttingRepositoryPort;
    }

    @Override
    public ScrapRecord save(ScrapRecord record) {
        ScrapRecordJpaEntity saved = repo.save(mapper.toNewEntity(record));
        return mapper.toDomain(saved);
    }

    @Override
    public List<ScrapResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end) {
        return repo.findByOperatorAndWindowRaw(operatorId, start, end)
                .stream()
                .map(row -> new ScrapResult(
                        ((Number) row[0]).longValue(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).intValue(),
                        (String) row[3],
                        row[4] instanceof LocalDateTime ldt ? ldt : ((java.sql.Timestamp) row[4]).toLocalDateTime()
                ))
                .toList();
    }

    @Override
    public Optional<ScrapResult> findById(Long id) {
        return repo.findById(id).map(entity -> new ScrapResult(
                entity.getId(),
                entity.getCuttingRecordId(),
                entity.getQuantity(),
                entity.getReason(),
                entity.getCreatedAt()
        ));
    }

    @Override
    public boolean existsById(Long cuttingRecordId) {
        return cuttingRepositoryPort.existsById(cuttingRecordId);
    }
}
