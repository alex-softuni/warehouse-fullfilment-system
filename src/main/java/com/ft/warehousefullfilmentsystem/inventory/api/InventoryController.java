package com.ft.warehousefullfilmentsystem.inventory.api;

import com.ft.warehousefullfilmentsystem.common.exception.ApiError;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.*;
import com.ft.warehousefullfilmentsystem.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@Tag(
        name = "Inventory",
        description = "Manage stock quantities and inventory transaction history"
)
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(
            summary = "Get inventory by product ID",
            description = "Returns available, reserved, and total physical stock for a product."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found for the supplied product ID",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @GetMapping("/product/{productId}")
    public InventoryResponse getInventoryByProductId(@PathVariable UUID productId) {
        return inventoryService.getInventoryByProductId(productId);
    }

    @Operation(
            summary = "Receive stock",
            description = "Adds newly received units to the product's available inventory."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock received successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found for the supplied product ID",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Receiving the quantity would overflow the inventory",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PostMapping("/receive")
    public InventoryResponse receiveStock(@Valid @RequestBody ReceiveStockRequest request) {
        return inventoryService.receiveStock(request);
    }

    @Operation(
            summary = "Get inventory transaction history",
            description = "Returns the recorded stock movements for the supplied product."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory transaction history retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found for the supplied product ID",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @GetMapping("/product/{productId}/transactions")
    public List<InventoryTransactionResponse> getTransactionHistory(@PathVariable UUID productId) {
        return inventoryService.getTransactionHistory(productId);
    }

    @Operation(
            summary = "Reserve stock",
            description = "Moves units from available inventory into reserved inventory."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock reserved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found for the supplied product ID",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient available stock",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PostMapping("/reserve")
    public InventoryResponse reserveStock(
            @Valid @RequestBody ReserveStockRequest request
    ) {
        return inventoryService.reserveStock(request);
    }

    @Operation(
            summary = "Release reserved stock",
            description = "Returns reserved units back to available inventory."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserved stock released successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found for the supplied product ID",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient reserved stock",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PostMapping("/release")
    public InventoryResponse releaseReservedStock(
            @Valid @RequestBody ReleaseStockRequest request
    ) {
        return inventoryService.releaseReservedStock(request);
    }

    @Operation(
            summary = "Ship reserved stock",
            description = "Removes shipped units from reserved physical inventory."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserved stock shipped successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found for the supplied product ID",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient reserved stock",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PostMapping("/ship")
    public InventoryResponse shipStock(
            @Valid @RequestBody ShipStockRequest request
    ) {
        return inventoryService.shipStock(request);
    }

}
