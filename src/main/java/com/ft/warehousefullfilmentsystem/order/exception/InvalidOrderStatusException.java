package com.ft.warehousefullfilmentsystem.order.exception;

import com.ft.warehousefullfilmentsystem.order.domain.OrderStatus;

import java.util.UUID;

public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(
            UUID orderId,
            OrderStatus currentStatus,
            String operation
    ) {
        super(
                "Cannot " + operation + " order " + orderId
                        + " because its status is " + currentStatus
        );
    }
}

