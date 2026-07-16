package com.ft.warehousefullfilmentsystem.inventory.api;

import com.ft.warehousefullfilmentsystem.inventory.api.dto.*;
import com.ft.warehousefullfilmentsystem.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/product/{productId}")
    public InventoryResponse getInventoryByProductId(@PathVariable UUID productId) {
        return inventoryService.getInventoryByProductId(productId);
    }

    @PostMapping("/receive")
    public InventoryResponse receiveStock(@Valid @RequestBody ReceiveStockRequest request) {
        return inventoryService.receiveStock(request);
    }

    @GetMapping("/product/{productId}/transactions")
    public List<InventoryTransactionResponse> getTransactionHistory(@PathVariable UUID productId) {
        return inventoryService.getTransactionHistory(productId);
    }

    @PostMapping("/reserve")
    public InventoryResponse reserveStock(
            @Valid @RequestBody ReserveStockRequest request
    ) {
        return inventoryService.reserveStock(request);
    }

    @PostMapping("/release")
    public InventoryResponse releaseReservedStock(
            @Valid @RequestBody ReleaseStockRequest request
    ) {
        return inventoryService.releaseReservedStock(request);
    }

}
