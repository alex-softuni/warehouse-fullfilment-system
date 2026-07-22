package com.ft.warehousefullfilmentsystem.order.api.dto;


import com.ft.warehousefullfilmentsystem.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record OrderResponse(
        UUID orderId,
        OrderStatus status,
        String customerName,
        String customerEmail,
        DeliveryAddressResponse address,
        List<OrderItemResponse> items,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
