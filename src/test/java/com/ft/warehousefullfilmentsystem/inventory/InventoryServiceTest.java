package com.ft.warehousefullfilmentsystem.inventory;


import com.ft.warehousefullfilmentsystem.inventory.api.dto.ReleaseStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.domain.Inventory;
import com.ft.warehousefullfilmentsystem.inventory.domain.InventoryTransaction;
import com.ft.warehousefullfilmentsystem.inventory.domain.InventoryTransactionType;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.InventoryResponse;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.ReceiveStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.ReserveStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.exception.InsufficientReservedStockException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InsufficientStockException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InventoryNotFoundException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InventoryOverflowException;
import com.ft.warehousefullfilmentsystem.inventory.repository.InventoryRepository;
import com.ft.warehousefullfilmentsystem.inventory.repository.InventoryTransactionRepository;
import com.ft.warehousefullfilmentsystem.inventory.service.InventoryService;
import com.ft.warehousefullfilmentsystem.product.Product;
import com.ft.warehousefullfilmentsystem.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {
    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private ProductRepository productRepository;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                inventoryRepository,
                transactionRepository,
                productRepository
        );
    }

    @Test
    void shouldIncreaseAvailableQuantityWhenStockIsReceived() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setSku("MONITOR-001");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);

        ReceiveStockRequest request =
                new ReceiveStockRequest(productId, 5);


        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        InventoryResponse response =
                inventoryService.receiveStock(request);

        ArgumentCaptor<InventoryTransaction> transactionCaptor =
                ArgumentCaptor.forClass(InventoryTransaction.class);

        verify(transactionRepository)
                .save(transactionCaptor.capture());

        InventoryTransaction savedTransaction =
                transactionCaptor.getValue();

        assertEquals(
                InventoryTransactionType.STOCK_RECEIVED,
                savedTransaction.getType()
        );

        assertEquals(15, response.availableQuantity());
    }

    @Test
    void shouldThrowExceptionWhenInventoryDoesNotExist() {
        UUID productId = UUID.randomUUID();
        ReceiveStockRequest request = new ReceiveStockRequest(productId, 5);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.receiveStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(transactionRepository, never())
                .save(any(InventoryTransaction.class));
    }

    @Test
    void shouldThrowExceptionWhenQuantityOverflows() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setSku("MONITOR-001");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(Integer.MAX_VALUE);
        inventory.setReservedQuantity(0);

        ReceiveStockRequest request =
                new ReceiveStockRequest(productId, 1);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InventoryOverflowException.class,
                () -> inventoryService.receiveStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(transactionRepository, never())
                .save(any(InventoryTransaction.class));
    }

    @Test
    void shouldMoveAvailableStockToReservedStock() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setSku("MONITOR-001");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(2);

        ReserveStockRequest request =
                new ReserveStockRequest(productId, 3);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        InventoryResponse response =
                inventoryService.reserveStock(request);

        assertEquals(7, response.availableQuantity());
        assertEquals(5, response.reservedQuantity());
        assertEquals(12, response.physicalQuantity());

        ArgumentCaptor<InventoryTransaction> transactionCaptor =
                ArgumentCaptor.forClass(InventoryTransaction.class);

        verify(transactionRepository)
                .save(transactionCaptor.capture());

        InventoryTransaction savedTransaction =
                transactionCaptor.getValue();

        assertEquals(
                InventoryTransactionType.STOCK_RESERVED,
                savedTransaction.getType()
        );

        assertEquals(3, savedTransaction.getQuantity());
        assertEquals(product, savedTransaction.getProduct());
    }

    @Test
    void shouldThrowExceptionWhenAvailableStockIsInsufficient() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setSku("MONITOR-001");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(2);
        inventory.setReservedQuantity(1);

        ReserveStockRequest request =
                new ReserveStockRequest(productId, 5);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reserveStock(request)
        );

        assertEquals(2, inventory.getAvailableQuantity());
        assertEquals(1, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(transactionRepository, never())
                .save(any(InventoryTransaction.class));
    }

    @Test
    void shouldThrowExceptionWhenReservingMissingInventory() {
        UUID productId = UUID.randomUUID();
        ReserveStockRequest request =
                new ReserveStockRequest(productId, 3);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.reserveStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(transactionRepository, never())
                .save(any(InventoryTransaction.class));
    }

    @Test
    void shouldThrowExceptionWhenReservedQuantityOverflows() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setSku("MONITOR-001");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(Integer.MAX_VALUE);

        ReserveStockRequest request =
                new ReserveStockRequest(productId, 1);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InventoryOverflowException.class,
                () -> inventoryService.reserveStock(request)
        );

        assertEquals(10, inventory.getAvailableQuantity());
        assertEquals(Integer.MAX_VALUE, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(transactionRepository, never())
                .save(any(InventoryTransaction.class));
    }

    @Test
    void shouldMoveReservedStockBackToAvailableStock() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setSku("MONITOR-001");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(7);
        inventory.setReservedQuantity(3);

        ReleaseStockRequest request =
                new ReleaseStockRequest(productId, 2);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        InventoryResponse response =
                inventoryService.releaseReservedStock(request);

        assertEquals(9, response.availableQuantity());
        assertEquals(1, response.reservedQuantity());
        assertEquals(10, response.physicalQuantity());

        ArgumentCaptor<InventoryTransaction> transactionCaptor =
                ArgumentCaptor.forClass(InventoryTransaction.class);

        verify(transactionRepository)
                .save(transactionCaptor.capture());

        InventoryTransaction savedTransaction =
                transactionCaptor.getValue();

        assertEquals(
                InventoryTransactionType.RESERVATION_RELEASED,
                savedTransaction.getType()
        );

        assertEquals(2, savedTransaction.getQuantity());
        assertEquals(product, savedTransaction.getProduct());
    }

    @Test
    void shouldThrowExceptionWhenReservedStockIsInsufficient() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setSku("MONITOR-001");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(7);
        inventory.setReservedQuantity(2);

        ReleaseStockRequest request =
                new ReleaseStockRequest(productId, 5);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientReservedStockException.class,
                () -> inventoryService.releaseReservedStock(request)
        );

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(2, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(transactionRepository, never())
                .save(any(InventoryTransaction.class));
    }

    @Test
    void shouldThrowExceptionWhenReleasingFromMissingInventory() {
        UUID productId = UUID.randomUUID();

        ReleaseStockRequest request =
                new ReleaseStockRequest(productId, 2);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.releaseReservedStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(transactionRepository, never())
                .save(any(InventoryTransaction.class));
    }

    @Test
    void shouldThrowExceptionWhenAvailableQuantityOverflowsDuringRelease() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setSku("MONITOR-001");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(Integer.MAX_VALUE);
        inventory.setReservedQuantity(1);

        ReleaseStockRequest request =
                new ReleaseStockRequest(productId, 1);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InventoryOverflowException.class,
                () -> inventoryService.releaseReservedStock(request)
        );

        assertEquals(
                Integer.MAX_VALUE,
                inventory.getAvailableQuantity()
        );

        assertEquals(1, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(transactionRepository, never())
                .save(any(InventoryTransaction.class));
    }
}

