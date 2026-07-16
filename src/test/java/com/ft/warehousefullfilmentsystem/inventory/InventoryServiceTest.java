package com.ft.warehousefullfilmentsystem.inventory;


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
}

