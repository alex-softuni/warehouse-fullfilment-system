package com.ft.warehousefullfilmentsystem.order.api;

import com.ft.warehousefullfilmentsystem.order.api.dto.CreateOrderRequest;
import com.ft.warehousefullfilmentsystem.order.api.dto.OrderResponse;
import com.ft.warehousefullfilmentsystem.order.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {


    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        Page<OrderResponse> responses = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {

        OrderResponse response = orderService.createOrder(createOrderRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderId") UUID orderId) {

        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("orderId") UUID orderId) {

        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable("orderId") UUID orderId) {

        OrderResponse response = orderService.shipOrder(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
