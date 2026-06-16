package com.example.company.molding.adapter.out.persistence;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.molding.domain.exception.MoldingOutputNotFoundException;
import com.example.company.molding.domain.model.MoldingOutput;
import com.example.company.molding.domain.port.out.MoldingOutputRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class MoldingOutputPersistenceAdapter implements MoldingOutputRepositoryPort {

    private final MoldingOutputSpringRepository moldingRepository;

    public MoldingOutputPersistenceAdapter(MoldingOutputSpringRepository moldingRepository) {
        this.moldingRepository = moldingRepository;
    }

    @Override
    public MoldingOutput save(MoldingOutput output) {
        MoldingOutputJpaEntity entity = new MoldingOutputJpaEntity(
                output.cuttingRecordId(),
                output.quantitySent(),
                output.operatorId()
        );
        return toDomain(moldingRepository.save(entity));
    }

    @Override
    public Optional<MoldingOutput> findById(Long id) {
        return moldingRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<MoldingOutput> findByOperatorAndTimeWindow(Long operatorId, LocalTime startTime,
                                                           LocalTime endTime, boolean overnight) {
        List<MoldingOutputJpaEntity> entities = overnight
                ? moldingRepository.findByOperatorAndOvernightWindow(operatorId, startTime, endTime)
                : moldingRepository.findByOperatorAndSameDayWindow(operatorId, startTime, endTime);
        return entities.stream().map(this::toDomain).toList();
    }

    private MoldingOutput toDomain(MoldingOutputJpaEntity entity) {
        return MoldingOutput.restore(
                entity.getId(),
                entity.getCuttingRecordId(),
                entity.getQuantitySent(),
                entity.getOperatorId(),
                entity.getSentAt()
        );
    }
}