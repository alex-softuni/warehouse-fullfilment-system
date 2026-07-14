package com.ft.warehousefullfilmentsystem.inventory;


import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID productId,
        String productSku,
        int availableQuantity,
        int reservedQuantity,
        int physicalQuantity
) {
}
