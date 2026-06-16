package com.example.company.scrap.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import com.example.company.scrap.domain.exception.ScrapNotFoundException;
import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class ScrapPersistenceAdapter implements ScrapRepositoryPort {

    private final ScrapSpringRepository scrapRepository;

    public ScrapPersistenceAdapter(ScrapSpringRepository scrapRepository) {
        this.scrapRepository = scrapRepository;
    }

    @Override
    public ScrapRecord save(ScrapRecord record) {
        ScrapRecordJpaEntity entity = new ScrapRecordJpaEntity(
                record.cuttingRecordId(),
                record.quantity(),
                record.reason()
        );
        return toDomain(scrapRepository.save(entity));
    }

    @Override
    public Optional<ScrapRecord> findById(Long id) {
        return scrapRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ScrapRecord> findByOperatorAndShift(Long operatorId, Long shiftId) {
        return scrapRepository.findByOperatorAndShift(operatorId, shiftId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ScrapRecord toDomain(ScrapRecordJpaEntity entity) {
        return ScrapRecord.restore(
                entity.getId(),
                entity.getCuttingRecordId(),
                entity.getQuantity(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}