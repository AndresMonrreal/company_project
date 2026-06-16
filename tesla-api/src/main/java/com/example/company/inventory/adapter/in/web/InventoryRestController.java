package com.example.company.inventory.adapter.in.web;

import com.example.company.inventory.adapter.in.web.dto.AvailableInventoryResponse;
import com.example.company.inventory.domain.model.AvailableInventoryResult;
import com.example.company.inventory.domain.port.in.GetAvailableInventoryUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryRestController {

    private final GetAvailableInventoryUseCase getAvailableInventory;

    public InventoryRestController(GetAvailableInventoryUseCase getAvailableInventory) {
        this.getAvailableInventory = getAvailableInventory;
    }

    @GetMapping("/available")
    public AvailableInventoryResponse findAvailable(@RequestParam String containerCode) {
        AvailableInventoryResult result = getAvailableInventory.findAvailableByContainerCode(containerCode);
        return new AvailableInventoryResponse(
                result.inventoryItemId(),
                result.containerCode(),
                result.profileCode(),
                result.lot(),
                result.availableQuantity()
        );
    }
}