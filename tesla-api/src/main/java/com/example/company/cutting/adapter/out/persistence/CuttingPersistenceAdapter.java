package com.example.company.cutting.adapter.out.persistence;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.cutting.domain.model.AvailableCuttingResult;
import com.example.company.cutting.domain.model.CuttingQuantities;
import com.example.company.cutting.domain.model.CuttingRecord;
import com.example.company.cutting.domain.port.in.CuttingResult;
import com.example.company.cutting.domain.port.out.CuttingRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class CuttingPersistenceAdapter implements CuttingRepositoryPort {

    private final CuttingSpringRepository cuttingRepository;

    public CuttingPersistenceAdapter(CuttingSpringRepository cuttingRepository) {
        this.cuttingRepository = cuttingRepository;
    }

    @Override
    public CuttingRecord save(CuttingRecord record) {
        CuttingRecordJpaEntity entity = new CuttingRecordJpaEntity(
                record.inventoryItemId(),
                record.machineId(),
                record.operatorId(),
                record.shiftId(),
                record.quantities().initialQuantity(),
                record.quantities().goodQuantity(),
                record.quantities().scrapQuantity()
        );
        return toDomain(cuttingRepository.save(entity));
    }

    @Override
    public Optional<CuttingRecord> findById(Long id) {
        return cuttingRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<CuttingRecord> findByOperatorAndShift(Long operatorId, Long shiftId) {
        return cuttingRepository.findByOperatorIdAndShiftId(operatorId, shiftId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<AvailableCuttingResult> findAvailableByContainerCode(String containerCode) {
        return cuttingRepository.findAvailableByContainerCode(containerCode)
                .map(this::mapToAvailableResult);
    }

    @Override
    public Optional<CuttingResult> findByIdWithCodes(Long id) {
        return cuttingRepository.findByIdWithCodes(id).map(this::mapToCuttingResult);
    }

    private CuttingRecord toDomain(CuttingRecordJpaEntity entity) {
        return CuttingRecord.restore(
                entity.getId(),
                entity.getInventoryItemId(),
                entity.getMachineId(),
                entity.getOperatorId(),
                entity.getShiftId(),
                new CuttingQuantities(entity.getInitialQuantity(), entity.getGoodQuantity(),
                        entity.getScrapQuantity()),
                entity.getCutAt()
        );
    }

    private AvailableCuttingResult mapToAvailableResult(Object[] row) {
        Object[] cols = (row[0] instanceof Object[]) ? (Object[]) row[0] : row;
        LocalDateTime cutAt = cols[6] instanceof Timestamp ts ? ts.toLocalDateTime() : (LocalDateTime) cols[6];
        return new AvailableCuttingResult(
                ((Number) cols[0]).longValue(),
                (String) cols[1],
                (String) cols[2],
                ((Number) cols[3]).intValue(),
                ((Number) cols[4]).intValue(),
                ((Number) cols[5]).intValue(),
                cutAt
        );
    }

    private CuttingResult mapToCuttingResult(Object[] row) {
        Object[] r = (row[0] instanceof Object[]) ? (Object[]) row[0] : row;
        LocalDateTime cutAt = r[4] instanceof Timestamp ts ? ts.toLocalDateTime() : (LocalDateTime) r[4];
        return new CuttingResult(
                ((Number) r[0]).longValue(),
                ((Number) r[8]).longValue(),
                ((Number) r[9]).longValue(),
                ((Number) r[10]).longValue(),
                ((Number) r[11]).longValue(),
                ((Number) r[1]).intValue(),
                ((Number) r[2]).intValue(),
                ((Number) r[3]).intValue(),
                cutAt,
                (String) r[5],
                (String) r[6],
                (String) r[7]
        );
    }
}
