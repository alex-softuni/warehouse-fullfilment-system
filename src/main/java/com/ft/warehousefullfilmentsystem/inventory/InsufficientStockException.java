package com.ft.warehousefullfilmentsystem.inventory;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(
            UUID productId,
            int requestedQuantity,
            int availableQuantity
    ) {
        super(
                "Insufficient stock for product with id '" + productId +
                        "'. Requested: " + requestedQuantity +
                        ", available: " + availableQuantity + "."
        );
    }
}
