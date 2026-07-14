package com.ft.warehousefullfilmentsystem.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;


    }

    @Transactional
    public InventoryResponse receiveStock(ReceiveStockRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductId(request.productId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(request.productId())
                );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + request.quantity()
        );

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return toResponse(updatedInventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        return toResponse(inventory);
    }

    private InventoryResponse toResponse(Inventory inventory) {
        int physicalQuantity =
                inventory.getAvailableQuantity()
                        + inventory.getReservedQuantity();

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                physicalQuantity
        );
    }
}
