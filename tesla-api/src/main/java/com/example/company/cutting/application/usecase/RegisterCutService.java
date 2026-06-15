package com.example.company.cutting.application.usecase;

import com.example.company.cutting.domain.model.CuttingRecord;
import com.example.company.cutting.domain.port.in.CuttingResult;
import com.example.company.cutting.domain.port.in.RegisterCutCommand;
import com.example.company.cutting.domain.port.in.RegisterCutUseCase;
import com.example.company.cutting.domain.port.out.CuttingRepositoryPort;
import com.example.company.inventory.domain.port.out.InventoryItemUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterCutService implements RegisterCutUseCase {

    private final CuttingRepositoryPort cuttingRepository;
    private final InventoryItemUpdatePort inventoryItemUpdatePort;

    public RegisterCutService(CuttingRepositoryPort cuttingRepository,
                              InventoryItemUpdatePort inventoryItemUpdatePort) {
        this.cuttingRepository = cuttingRepository;
        this.inventoryItemUpdatePort = inventoryItemUpdatePort;
    }

    @Override
    @Transactional
    public CuttingResult register(RegisterCutCommand command) {
        CuttingRecord record = CuttingRecord.create(
                command.inventoryItemId(),
                command.machineId(),
                command.operatorId(),
                command.shiftId(),
                command.initialQuantity(),
                command.goodQuantity(),
                command.scrapQuantity()
        );

        CuttingResult result = cuttingRepository.save(record);

        inventoryItemUpdatePort.updateToCut(command.inventoryItemId(), command.goodQuantity());

        return result;
    }
}
