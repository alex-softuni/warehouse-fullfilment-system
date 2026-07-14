package com.ft.warehousefullfilmentsystem.inventory;

import java.util.UUID;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(UUID productId) {
        super("Inventory for product with id '" + productId + "' was not found.");
    }
}
