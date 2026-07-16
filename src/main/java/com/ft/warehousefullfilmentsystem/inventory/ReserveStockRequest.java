package com.ft.warehousefullfilmentsystem.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ReserveStockRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,

        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {
}
