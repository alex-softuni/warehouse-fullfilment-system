package com.ft.warehousefullfilmentsystem.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateOrderRequest(

        @NotBlank(message = "Customer name is required")
        @Size(min = 2, max = 40, message = "Name must be between 2 and 40 characters")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Email must be valid")
        String customerEmail,

        @Valid
        @NotNull(message = "Delivery address is required")
        DeliveryAddressRequest address,

        @Valid
        @NotEmpty(message = "Order must contain at least one item")
        @Size(max = 100, message = "Order cannot contain more than 100 items")
        List<OrderItemRequest> items

) {
}
