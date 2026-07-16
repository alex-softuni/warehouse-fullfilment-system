package com.ft.warehousefullfilmentsystem.inventory.service;

import com.ft.warehousefullfilmentsystem.inventory.exception.InsufficientStockException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InventoryNotFoundException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InventoryOverflowException;
import com.ft.warehousefullfilmentsystem.inventory.domain.InventoryTransactionType;
import com.ft.warehousefullfilmentsystem.inventory.domain.Inventory;
import com.ft.warehousefullfilmentsystem.inventory.domain.InventoryTransaction;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.InventoryResponse;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.InventoryTransactionResponse;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.ReceiveStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.ReserveStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.repository.InventoryRepository;
import com.ft.warehousefullfilmentsystem.inventory.repository.InventoryTransactionRepository;
import com.ft.warehousefullfilmentsystem.product.ProductNotFoundException;
import com.ft.warehousefullfilmentsystem.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, InventoryTransactionRepository inventoryTransactionRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryResponse receiveStock(ReceiveStockRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductId(request.productId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(request.productId())
                );

        int updatedQuantity;

        try {
            updatedQuantity = Math.addExact(
                    inventory.getAvailableQuantity(),
                    request.quantity()
            );
        } catch (ArithmeticException e) {
            throw new InventoryOverflowException(request.productId());
        }
        
        inventory.setAvailableQuantity(updatedQuantity);

        Inventory updatedInventory = inventoryRepository.save(inventory);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(inventory.getProduct());
        transaction.setType(InventoryTransactionType.STOCK_RECEIVED);
        transaction.setQuantity(request.quantity());
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        return toResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse reserveStock(ReserveStockRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductId(request.productId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(request.productId())
                );

        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new InsufficientStockException(
                    request.productId(),
                    request.quantity(),
                    inventory.getAvailableQuantity()
            );
        }



        int updatedReservedQuantity;

        try {
            updatedReservedQuantity = Math.addExact(
                    inventory.getReservedQuantity(),
                    request.quantity()
            );
        } catch (ArithmeticException exception) {
            throw new InventoryOverflowException(request.productId());
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.quantity());
        inventory.setReservedQuantity(updatedReservedQuantity);


        Inventory updatedInventory = inventoryRepository.save(inventory);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(inventory.getProduct());
        transaction.setType(InventoryTransactionType.STOCK_RESERVED);
        transaction.setQuantity(request.quantity());
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        return toResponse(updatedInventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        return toResponse(inventory);
    }

    private InventoryResponse toResponse(Inventory inventory) {
        int physicalQuantity =
                inventory.getAvailableQuantity()
                        + inventory.getReservedQuantity();

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                physicalQuantity
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactionHistory(
            UUID productId
    ) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return transactionRepository
                .findAllByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    private InventoryTransactionResponse toTransactionResponse(InventoryTransaction transaction) {
        return new InventoryTransactionResponse(
                transaction.getId(),
                transaction.getProduct().getId(),
                transaction.getProduct().getSku(),
                transaction.getType(),
                transaction.getQuantity(),
                transaction.getCreatedAt()
        );
    }
}
