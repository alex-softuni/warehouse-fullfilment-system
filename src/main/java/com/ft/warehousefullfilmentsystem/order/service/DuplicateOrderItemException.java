package com.ft.warehousefullfilmentsystem.order.service;

import java.util.UUID;

public class DuplicateOrderItemException extends RuntimeException {
    public DuplicateOrderItemException(UUID productId) {
        super("Duplicate Order Item ID: " + productId);
    }
}
