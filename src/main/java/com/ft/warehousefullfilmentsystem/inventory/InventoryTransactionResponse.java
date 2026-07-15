package com.ft.warehousefullfilmentsystem.inventory;

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
