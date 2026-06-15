package com.example.company.reception.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.reception.domain.model.Reception;
import com.example.company.reception.domain.port.in.ReceptionResult;
import com.example.company.reception.domain.port.out.ReceptionRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
class ReceptionPersistenceAdapter implements ReceptionRepositoryPort {

    private final ReceptionSpringRepository repository;
    private final ReceptionPersistenceMapper mapper;

    ReceptionPersistenceAdapter(ReceptionSpringRepository repository, ReceptionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ReceptionResult save(Reception reception) {
        ReceptionJpaEntity saved = repository.save(mapper.toNewEntity(reception));

        String containerCode = repository.findContainerCodeById(saved.getContainerId()).orElse("");
        String profileCode = repository.findProfileCodeById(saved.getProfileId()).orElse("");

        return new ReceptionResult(
                saved.getId(),
                containerCode,
                profileCode,
                saved.getLot(),
                saved.getReceivedQuantity(),
                saved.getStatus(),
                saved.getReceivedAt()
        );
    }

    @Override
    public List<ReceptionResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end) {
        return repository.findByOperatorAndWindowRaw(operatorId, start, end)
                .stream()
                .map(this::rowToResult)
                .toList();
    }

    @Override
    public Optional<ReceptionResult> findById(Long id) {
        return repository.findByIdWithCodes(id).map(this::rowToResult);
    }

    private ReceptionResult rowToResult(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String lot = (String) row[1];
        int receivedQuantity = ((Number) row[2]).intValue();
        String status = (String) row[3];
        LocalDateTime receivedAt = toLocalDateTime(row[4]);
        String containerCode = (String) row[5];
        String profileCode = (String) row[6];

        return new ReceptionResult(id, containerCode, profileCode, lot, receivedQuantity, status, receivedAt);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        throw new IllegalStateException("Unexpected type for received_at column: " + value.getClass().getName());
    }
}
