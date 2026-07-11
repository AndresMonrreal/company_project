package com.example.company.scrap.adapter.out.persistence;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.ScrapResult;
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
                record.shiftId(),
                record.profileId(),
                record.operatorId(),
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
    public List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        return scrapRepository.findWithDetailsByOperatorAndShift(operatorId, shiftId)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private ScrapRecord toDomain(ScrapRecordJpaEntity entity) {
        return ScrapRecord.restore(
                entity.getId(),
                entity.getShiftId(),
                entity.getProfileId(),
                entity.getOperatorId(),
                entity.getQuantity(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }

    private ScrapResult toResult(Object[] row) {
        Object[] cols = (row[0] instanceof Object[]) ? (Object[]) row[0] : row;
        LocalDateTime createdAt = (cols[8] instanceof Timestamp ts)
                ? ts.toLocalDateTime()
                : (LocalDateTime) cols[8];
        return new ScrapResult(
                ((Number) cols[0]).longValue(),
                ((Number) cols[2]).longValue(),
                (String) cols[1],
                ((Number) cols[4]).longValue(),
                (String) cols[3],
                ((Number) cols[5]).longValue(),
                ((Number) cols[6]).intValue(),
                (String) cols[7],
                createdAt
        );
    }
}