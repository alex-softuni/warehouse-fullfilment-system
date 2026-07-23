package com.ft.warehousefullfilmentsystem.order.api;

import com.ft.warehousefullfilmentsystem.order.api.dto.CreateOrderRequest;
import com.ft.warehousefullfilmentsystem.order.api.dto.OrderResponse;
import com.ft.warehousefullfilmentsystem.order.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {


    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {

        OrderResponse response = orderService.createOrder(createOrderRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
