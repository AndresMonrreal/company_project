package com.example.company.cutting.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.company.cutting.domain.model.AvailableCuttingResult;
import com.example.company.cutting.domain.model.CuttingQuantities;
import com.example.company.cutting.domain.model.CuttingRecord;
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
        return new AvailableCuttingResult(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                ((Number) row[3]).intValue(),
                ((Number) row[4]).intValue(),
                ((Number) row[5]).intValue(),
                (LocalDateTime) row[6]
        );
    }
}