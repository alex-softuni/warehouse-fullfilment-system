package com.ft.warehousefullfilmentsystem.inventory.exception;

import java.util.UUID;

public class InsufficientReservedStockException extends RuntimeException {
    public InsufficientReservedStockException(
            UUID productId,
            int requestedQuantity,
            int reservedQuantity
    ) {
        super(
                "Cannot release reserved stock for product with id '" +
                        productId +
                        "'. Requested: " + requestedQuantity +
                        ", reserved: " + reservedQuantity + "."
        );
    }
}
