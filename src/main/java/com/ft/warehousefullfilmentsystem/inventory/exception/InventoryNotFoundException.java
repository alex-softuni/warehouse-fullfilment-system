package com.ft.warehousefullfilmentsystem.inventory.exception;

import java.util.UUID;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(UUID productId) {
        super("Inventory for product with id '" + productId + "' was not found.");
    }
}
