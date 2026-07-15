package com.ft.warehousefullfilmentsystem.inventory;

import java.util.UUID;

public class InventoryOverflowException extends RuntimeException {
    public InventoryOverflowException(UUID productId) {
        super(
                "Receiving stock would exceed the maximum supported quantity " +
                        "for product with id '" + productId + "'."
        );
    }
}
