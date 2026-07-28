package com.ft.warehousefullfilmentsystem.product.api;

import com.ft.warehousefullfilmentsystem.common.exception.ApiError;
import com.ft.warehousefullfilmentsystem.product.api.dto.ProductRequest;
import com.ft.warehousefullfilmentsystem.product.api.dto.ProductResponse;
import com.ft.warehousefullfilmentsystem.product.api.dto.UpdateProductRequest;
import com.ft.warehousefullfilmentsystem.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(
        name = "Products",
        description = "Create, retrieve, update, archive, and restore products"
)
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;

    }

    @Operation(
            summary = "Create a product",
            description = "Creates a new active product and automatically creates its inventory record."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A product with the supplied SKU already exists",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    @Operation(
            summary = "Get active products",
            description = "Returns all products that are currently active."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Active products retrieved successfully"
    )
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @Operation(
            summary = "Get a product by ID",
            description = "Returns the product matching the supplied UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @Operation(
            summary = "Update a product",
            description = "Updates the product name and price. The SKU remains unchanged."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated successfully"
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
                    description = "Product not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @Operation(
            summary = "Archive a product",
            description = "Performs a soft deletion by marking the product as inactive."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product archived successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PatchMapping("/{id}/archive")
    public ProductResponse archiveProduct(@PathVariable UUID id) {
        return productService.archiveProduct(id);
    }

    @Operation(
            summary = "Get archived products",
            description = "Returns all products that are currently inactive."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Archived products retrieved successfully"
    )
    @GetMapping("/archived")
    public List<ProductResponse> getArchivedProducts() {
        return productService.getArchivedProducts();
    }

    @Operation(
            summary = "Restore an archived product",
            description = "Marks an archived product as active again."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product restored successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PatchMapping("/{id}/restore")
    public ProductResponse restoreProduct(@PathVariable UUID id) {
        return productService.restoreProduct(id);
    }
}
