package com.example.company.molding.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.cutting.domain.port.out.CuttingRepositoryPort;
import com.example.company.molding.domain.model.MoldingOutput;
import com.example.company.molding.domain.port.in.MoldingOutputResult;
import com.example.company.molding.domain.port.out.CuttingRecordExistsPort;
import com.example.company.molding.domain.port.out.MoldingOutputRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
class MoldingOutputPersistenceAdapter implements MoldingOutputRepositoryPort, CuttingRecordExistsPort {

    private final MoldingOutputSpringRepository springRepository;
    private final MoldingOutputPersistenceMapper mapper;
    private final CuttingRepositoryPort cuttingRepositoryPort;

    MoldingOutputPersistenceAdapter(
            MoldingOutputSpringRepository springRepository,
            MoldingOutputPersistenceMapper mapper,
            CuttingRepositoryPort cuttingRepositoryPort
    ) {
        this.springRepository = springRepository;
        this.mapper = mapper;
        this.cuttingRepositoryPort = cuttingRepositoryPort;
    }

    @Override
    public MoldingOutput save(MoldingOutput output) {
        MoldingOutputJpaEntity entity = mapper.toNewEntity(output);
        MoldingOutputJpaEntity saved = springRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<MoldingOutputResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end) {
        return springRepository.findByOperatorAndWindow(operatorId, start, end)
                .stream()
                .map(entity -> new MoldingOutputResult(
                        entity.getId(),
                        entity.getCuttingRecordId(),
                        entity.getQuantitySent(),
                        entity.getSentAt()
                ))
                .toList();
    }

    @Override
    public Optional<MoldingOutputResult> findById(Long id) {
        return springRepository.findById(id).map(entity -> new MoldingOutputResult(
                entity.getId(),
                entity.getCuttingRecordId(),
                entity.getQuantitySent(),
                entity.getSentAt()
        ));
    }

    @Override
    public boolean existsById(Long cuttingRecordId) {
        return cuttingRepositoryPort.existsById(cuttingRecordId);
    }
}
