package com.ft.warehousefullfilmentsystem.order.api;

import com.ft.warehousefullfilmentsystem.common.exception.ApiError;
import com.ft.warehousefullfilmentsystem.order.api.dto.CreateOrderRequest;
import com.ft.warehousefullfilmentsystem.order.api.dto.OrderResponse;
import com.ft.warehousefullfilmentsystem.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(
        name = "Orders",
        description = "Create, retrieve, cancel, and ship customer orders"
)
public class OrderController {


    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(
            summary = "Get all orders",
            description = "Returns orders using pagination, sorted by creation date descending by default."
    )
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

    @Operation(
            summary = "Create an order",
            description = "Creates a multi-item order, reserves stock, and stores product details as snapshots."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created and stock reserved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed or the order contains duplicate products",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "A requested product or inventory record was not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient available stock for one or more order items",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {

        OrderResponse response = orderService.createOrder(createOrderRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get an order by ID",
            description = "Returns the order matching the supplied UUID, including its items and delivery address."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {

        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "Cancel an order",
            description = "Cancels a confirmed order and releases its reserved stock back into available inventory."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled and reserved stock released successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Order cannot be cancelled from its current status",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("orderId") UUID orderId) {

        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "Ship an order",
            description = "Ships a confirmed order and removes its reserved units from physical inventory."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order shipped and reserved stock removed successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Order cannot be shipped from its current status",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable("orderId") UUID orderId) {

        OrderResponse response = orderService.shipOrder(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
