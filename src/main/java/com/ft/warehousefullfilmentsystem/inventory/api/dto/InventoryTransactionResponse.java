package com.ft.warehousefullfilmentsystem.inventory.api.dto;

import com.ft.warehousefullfilmentsystem.inventory.domain.InventoryTransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryTransactionResponse(
        UUID id,
        UUID productId,
        String productSku,
        InventoryTransactionType type,
        int quantity,
        LocalDateTime createdAt
) {
}
