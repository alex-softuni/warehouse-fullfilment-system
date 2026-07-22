package com.ft.warehousefullfilmentsystem.order.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OrderItemRequest(

        @NotNull(message = "Product ID is required")
        UUID productId,

        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {
}
