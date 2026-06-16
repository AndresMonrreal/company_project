package com.example.company.reception.application.usecase;

import com.example.company.inventory.domain.port.out.InventoryItemCreationPort;
import com.example.company.reception.domain.model.Reception;
import com.example.company.reception.domain.port.in.RegisterReceptionCommand;
import com.example.company.reception.domain.port.in.RegisterReceptionUseCase;
import com.example.company.reception.domain.port.in.ReceptionResult;
import com.example.company.reception.domain.port.out.ReceptionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterReceptionService implements RegisterReceptionUseCase {

    private final ReceptionRepositoryPort receptionRepository;
    private final InventoryItemCreationPort inventoryItemCreation;

    public RegisterReceptionService(ReceptionRepositoryPort receptionRepository,
                                    InventoryItemCreationPort inventoryItemCreation) {
        this.receptionRepository = receptionRepository;
        this.inventoryItemCreation = inventoryItemCreation;
    }

    @Override
    @Transactional
    public ReceptionResult register(RegisterReceptionCommand command) {
        Reception reception = Reception.create(
                command.containerId(),
                command.profileId(),
                command.operatorId(),
                command.lot(),
                command.receivedQuantity()
        );
        Reception saved = receptionRepository.save(reception);
        inventoryItemCreation.createInventoryItem(saved.id(), saved.receivedQuantity());
        return toResult(saved);
    }

    private ReceptionResult toResult(Reception reception) {
        return new ReceptionResult(
                reception.id(),
                reception.containerId(),
                reception.profileId(),
                reception.operatorId(),
                reception.lot(),
                reception.receivedQuantity(),
                reception.status().name(),
                reception.receivedAt()
        );
    }
}