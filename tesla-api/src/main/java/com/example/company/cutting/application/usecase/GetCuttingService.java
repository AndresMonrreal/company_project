package com.example.company.cutting.application.usecase;

import java.util.List;

import com.example.company.cutting.domain.exception.CuttingNotAvailableException;
import com.example.company.cutting.domain.model.AvailableCuttingResult;
import com.example.company.cutting.domain.model.CuttingRecord;
import com.example.company.cutting.domain.port.in.CuttingResult;
import com.example.company.cutting.domain.port.in.GetCuttingUseCase;
import com.example.company.cutting.domain.port.out.CuttingRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetCuttingService implements GetCuttingUseCase {

    private final CuttingRepositoryPort cuttingRepository;

    public GetCuttingService(CuttingRepositoryPort cuttingRepository) {
        this.cuttingRepository = cuttingRepository;
    }

    @Override
    public CuttingResult findById(Long id) {
        return cuttingRepository.findById(id)
                .map(this::toResult)
                .orElseThrow(() -> new IllegalArgumentException("Cutting record not found: " + id));
    }

    @Override
    public List<CuttingResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        return cuttingRepository.findByOperatorAndShift(operatorId, shiftId)
                .stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public AvailableCuttingResult findAvailableByContainerCode(String containerCode) {
        return cuttingRepository.findAvailableByContainerCode(containerCode)
                .orElseThrow(() -> new CuttingNotAvailableException(containerCode));
    }

    private CuttingResult toResult(CuttingRecord record) {
        return new CuttingResult(
                record.id(),
                record.inventoryItemId(),
                record.machineId(),
                record.operatorId(),
                record.shiftId(),
                record.quantities().initialQuantity(),
                record.quantities().goodQuantity(),
                record.quantities().scrapQuantity(),
                record.cutAt()
        );
    }
}