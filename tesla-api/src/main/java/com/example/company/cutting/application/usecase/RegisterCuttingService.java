package com.example.company.cutting.application.usecase;

import com.example.company.cutting.domain.exception.CuttingQuantityInvariantException;
import com.example.company.cutting.domain.model.CuttingQuantities;
import com.example.company.cutting.domain.model.CuttingRecord;
import com.example.company.cutting.domain.port.in.CuttingResult;
import com.example.company.cutting.domain.port.in.RegisterCuttingCommand;
import com.example.company.cutting.domain.port.in.RegisterCuttingUseCase;
import com.example.company.cutting.domain.port.out.CuttingRepositoryPort;
import com.example.company.inventory.domain.port.out.InventoryItemUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterCuttingService implements RegisterCuttingUseCase {

    private final CuttingRepositoryPort cuttingRepository;
    private final InventoryItemUpdatePort inventoryItemUpdate;

    public RegisterCuttingService(CuttingRepositoryPort cuttingRepository,
                                  InventoryItemUpdatePort inventoryItemUpdate) {
        this.cuttingRepository = cuttingRepository;
        this.inventoryItemUpdate = inventoryItemUpdate;
    }

    @Override
    @Transactional
    public CuttingResult register(RegisterCuttingCommand command) {
        CuttingQuantities quantities;
        try {
            quantities = new CuttingQuantities(command.initialQuantity(), command.goodQuantity(),
                    command.scrapQuantity());
        } catch (IllegalArgumentException e) {
            throw new CuttingQuantityInvariantException(e.getMessage());
        }

        CuttingRecord record = CuttingRecord.create(
                command.inventoryItemId(),
                command.machineId(),
                command.operatorId(),
                command.shiftId(),
                quantities
        );
        CuttingRecord saved = cuttingRepository.save(record);
        inventoryItemUpdate.markAsCut(command.inventoryItemId());
        return toResult(saved);
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
                record.cutAt(),
                null,
                null,
                null
        );
    }
}