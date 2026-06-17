package com.example.company.reception.adapter.out.persistence;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.reception.domain.exception.ReceptionNotFoundException;
import com.example.company.reception.domain.model.Reception;
import com.example.company.reception.domain.model.ReceptionStatus;
import com.example.company.reception.domain.port.in.ReceptionResult;
import com.example.company.reception.domain.port.out.ReceptionRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class ReceptionPersistenceAdapter implements ReceptionRepositoryPort {

    private final ReceptionSpringRepository receptionRepository;

    public ReceptionPersistenceAdapter(ReceptionSpringRepository receptionRepository) {
        this.receptionRepository = receptionRepository;
    }

    @Override
    public Reception save(Reception reception) {
        ReceptionJpaEntity entity = new ReceptionJpaEntity(
                reception.containerId(),
                reception.profileId(),
                reception.operatorId(),
                reception.lot(),
                reception.receivedQuantity(),
                reception.status().name()
        );
        ReceptionJpaEntity saved = receptionRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Reception> findById(Long id) {
        return receptionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Reception> findByOperatorAndTimeWindow(Long operatorId, LocalTime startTime,
                                                       LocalTime endTime, boolean overnight) {
        List<ReceptionJpaEntity> entities = overnight
                ? receptionRepository.findByOperatorAndOvernightWindow(operatorId, startTime, endTime)
                : receptionRepository.findByOperatorAndSameDayWindow(operatorId, startTime, endTime);
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ReceptionResult> findByIdWithCodes(Long id) {
        return receptionRepository.findByIdWithCodes(id).map(this::mapToReceptionResult);
    }

    private Reception toDomain(ReceptionJpaEntity entity) {
        return Reception.restore(
                entity.getId(),
                entity.getContainerId(),
                entity.getProfileId(),
                entity.getOperatorId(),
                entity.getLot(),
                entity.getReceivedQuantity(),
                ReceptionStatus.valueOf(entity.getStatus()),
                entity.getReceivedAt()
        );
    }

    private ReceptionResult mapToReceptionResult(Object[] row) {
        Object[] r = (row[0] instanceof Object[]) ? (Object[]) row[0] : row;
        return new ReceptionResult(
                ((Number) r[0]).longValue(),
                (String) r[5],
                (String) r[6],
                ((Number) r[9]).longValue(),
                (String) r[1],
                ((Number) r[2]).intValue(),
                (String) r[3],
                r[4] instanceof Timestamp ts ? ts.toLocalDateTime() : (java.time.LocalDateTime) r[4]
        );
    }
}