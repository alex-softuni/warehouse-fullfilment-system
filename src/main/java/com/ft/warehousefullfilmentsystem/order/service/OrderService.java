package com.ft.warehousefullfilmentsystem.order.service;

import com.ft.warehousefullfilmentsystem.inventory.api.dto.ReserveStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.service.InventoryService;
import com.ft.warehousefullfilmentsystem.order.api.dto.*;
import com.ft.warehousefullfilmentsystem.order.domain.DeliveryAddress;
import com.ft.warehousefullfilmentsystem.order.domain.Order;
import com.ft.warehousefullfilmentsystem.order.domain.OrderItem;
import com.ft.warehousefullfilmentsystem.order.domain.OrderStatus;
import com.ft.warehousefullfilmentsystem.order.exception.DuplicateOrderItemException;
import com.ft.warehousefullfilmentsystem.order.repository.OrderRepository;
import com.ft.warehousefullfilmentsystem.product.Product;
import com.ft.warehousefullfilmentsystem.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;


    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Set<UUID> productIds = new HashSet<>();
        for (OrderItemRequest item : request.items()) {
            if (!productIds.add(item.productId())) {
                throw new DuplicateOrderItemException(item.productId());
            }
        }

        Order order = mapToOrder(request);

        request.items().forEach(itemRequest -> {
            Product product = productService.getActiveProduct(itemRequest.productId());
            inventoryService.validateAvailableStock(
                    product.getId(),
                    itemRequest.quantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductSku(product.getSku());
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.quantity());

            order.addItem(orderItem);

        });

        order.getItems().forEach(orderItem ->
                inventoryService.reserveStock(new ReserveStockRequest(
                        orderItem.getProduct().getId(),
                        orderItem.getQuantity())));

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);

    }

    private static @NonNull Order mapToOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());

        DeliveryAddress address = new DeliveryAddress();
        address.setCountry(request.address().country());
        address.setCity(request.address().city());
        address.setPostalCode(request.address().postalCode());
        address.setStreet(request.address().street());
        address.setAddressLine(request.address().addressLine());

        order.setDeliveryAddress(address);
        return order;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal totalPrice = itemResponses.stream()
                .map(OrderItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DeliveryAddressResponse addressResponse = toAddressResponse(order.getDeliveryAddress());


        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                addressResponse,
                itemResponses,
                totalPrice,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }


    private DeliveryAddressResponse toAddressResponse(DeliveryAddress address) {
        return new DeliveryAddressResponse(
                address.getCountry(),
                address.getCity(),
                address.getPostalCode(),
                address.getStreet(),
                address.getAddressLine()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProductSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal
        );
    }
}
