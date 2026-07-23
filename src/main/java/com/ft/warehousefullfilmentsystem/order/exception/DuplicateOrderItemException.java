package com.ft.warehousefullfilmentsystem.order.exception;

import java.util.UUID;

public class DuplicateOrderItemException extends RuntimeException {
    public DuplicateOrderItemException(UUID productId) {
        super("Order contains duplicate product: " + productId);
    }
}
