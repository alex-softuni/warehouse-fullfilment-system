package com.ft.warehousefullfilmentsystem.Order;

import com.ft.warehousefullfilmentsystem.inventory.api.dto.ReleaseStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.ReserveStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.api.dto.ShipStockRequest;
import com.ft.warehousefullfilmentsystem.inventory.exception.InsufficientReservedStockException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InsufficientStockException;
import com.ft.warehousefullfilmentsystem.inventory.service.InventoryService;
import com.ft.warehousefullfilmentsystem.order.api.dto.CreateOrderRequest;
import com.ft.warehousefullfilmentsystem.order.api.dto.DeliveryAddressRequest;
import com.ft.warehousefullfilmentsystem.order.api.dto.OrderItemRequest;
import com.ft.warehousefullfilmentsystem.order.api.dto.OrderResponse;
import com.ft.warehousefullfilmentsystem.order.domain.DeliveryAddress;
import com.ft.warehousefullfilmentsystem.order.domain.Order;
import com.ft.warehousefullfilmentsystem.order.domain.OrderItem;
import com.ft.warehousefullfilmentsystem.order.domain.OrderStatus;
import com.ft.warehousefullfilmentsystem.order.exception.DuplicateOrderItemException;
import com.ft.warehousefullfilmentsystem.order.exception.InvalidOrderStatusException;
import com.ft.warehousefullfilmentsystem.order.exception.OrderNotFoundException;
import com.ft.warehousefullfilmentsystem.order.repository.OrderRepository;
import com.ft.warehousefullfilmentsystem.order.service.OrderService;
import com.ft.warehousefullfilmentsystem.product.Product;
import com.ft.warehousefullfilmentsystem.product.ProductNotFoundException;
import com.ft.warehousefullfilmentsystem.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private InventoryService inventoryService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                productService,
                inventoryService);
    }

    @Test
    void shouldCreateOrderSuccessfully() {

        UUID monitorId = UUID.randomUUID();
        UUID keyboardId = UUID.randomUUID();

        Product monitor = new Product();
        monitor.setId(monitorId);
        monitor.setSku("MONITOR-001");
        monitor.setName("Dell Monitor");
        monitor.setPrice(new BigDecimal("200.00"));
        monitor.setActive(true);

        Product keyboard = new Product();
        keyboard.setId(keyboardId);
        keyboard.setSku("KEYBOARD-001");
        keyboard.setName("Mechanical Keyboard");
        keyboard.setPrice(new BigDecimal("80.00"));
        keyboard.setActive(true);

        DeliveryAddressRequest address = new DeliveryAddressRequest(
                "Bulgaria",
                "Sofia",
                "1000",
                "Vitosha Boulevard",
                "Apartment 7"
        );

        CreateOrderRequest request = new CreateOrderRequest(
                "Alexander",
                "alexander@example.com",
                address,
                List.of(
                        new OrderItemRequest(monitorId, 2),
                        new OrderItemRequest(keyboardId, 1)
                )
        );

        when(productService.getActiveProduct(monitorId))
                .thenReturn(monitor);

        when(productService.getActiveProduct(keyboardId))
                .thenReturn(keyboard);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertEquals(OrderStatus.CONFIRMED, response.status());
        assertEquals(2, response.items().size());
        assertEquals(new BigDecimal("480.00"), response.totalPrice());

        verify(productService).getActiveProduct(monitorId);
        verify(productService).getActiveProduct(keyboardId);
        verify(inventoryService)
                .validateAvailableStock(monitorId, 2);
        verify(inventoryService)
                .validateAvailableStock(keyboardId, 1);
        verify(inventoryService)
                .reserveStock(new ReserveStockRequest(monitorId, 2));
        verify(inventoryService)
                .reserveStock(new ReserveStockRequest(keyboardId, 1));

        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderStatus.CONFIRMED, savedOrder.getStatus());
        assertEquals(2, savedOrder.getItems().size());

        OrderItem savedMonitorItem = savedOrder.getItems().get(0);

        assertEquals(monitorId, savedMonitorItem.getProduct().getId());
        assertEquals("MONITOR-001", savedMonitorItem.getProductSku());
        assertEquals("Dell Monitor", savedMonitorItem.getProductName());
        assertEquals(new BigDecimal("200.00"), savedMonitorItem.getUnitPrice());
        assertEquals(2, savedMonitorItem.getQuantity());
        assertSame(savedOrder, savedMonitorItem.getOrder());

        OrderItem savedKeyboardItem = savedOrder.getItems().get(1);

        assertEquals(keyboardId, savedKeyboardItem.getProduct().getId());
        assertEquals("KEYBOARD-001", savedKeyboardItem.getProductSku());
        assertEquals("Mechanical Keyboard", savedKeyboardItem.getProductName());
        assertEquals(new BigDecimal("80.00"), savedKeyboardItem.getUnitPrice());
        assertEquals(1, savedKeyboardItem.getQuantity());
        assertSame(savedOrder, savedKeyboardItem.getOrder());

        assertEquals("Alexander", savedOrder.getCustomerName());
        assertEquals("alexander@example.com", savedOrder.getCustomerEmail());

        assertEquals("Bulgaria", savedOrder.getDeliveryAddress().getCountry());
        assertEquals("Sofia", savedOrder.getDeliveryAddress().getCity());
        assertEquals("1000", savedOrder.getDeliveryAddress().getPostalCode());
        assertEquals("Vitosha Boulevard", savedOrder.getDeliveryAddress().getStreet());
        assertEquals("Apartment 7", savedOrder.getDeliveryAddress().getAddressLine());
    }

    @Test
    void shouldRejectOrderWithDuplicateProductIds() {

        UUID productId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                "Alexander",
                "alexander@example.com",
                new DeliveryAddressRequest(
                        "Bulgaria",
                        "Sofia",
                        "1000",
                        "Vitosha Boulevard",
                        "Apartment 7"
                ),
                List.of(
                        new OrderItemRequest(productId, 2),
                        new OrderItemRequest(productId, 1)
                )
        );

        assertThrows(
                DuplicateOrderItemException.class,
                () -> orderService.createOrder(request)
        );
        verifyNoInteractions(
                productService,
                inventoryService,
                orderRepository
        );
    }

    @Test
    void shouldRejectOrderWhenProductIsNotActiveOrMissing() {

        UUID productId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                "Alexander",
                "alexander@example.com",
                new DeliveryAddressRequest(
                        "Bulgaria",
                        "Sofia",
                        "1000",
                        "Vitosha Boulevard",
                        "Apartment 7"
                ),
                List.of(
                        new OrderItemRequest(productId, 2)
                )
        );

        when(productService.getActiveProduct(productId))
                .thenThrow(new ProductNotFoundException(productId));

        assertThrows(
                ProductNotFoundException.class,
                () -> orderService.createOrder(request)
        );
        verify(productService).getActiveProduct(productId);

        verifyNoInteractions(
                inventoryService,
                orderRepository
        );
    }

    @Test
    void shouldRejectOrderWhenAvailableStockIsInsufficient() {

        UUID productId = UUID.randomUUID();
        int requestedQuantity = 10;
        int availableQuantity = 5;

        Product product = new Product();
        product.setId(productId);
        product.setSku("MONITOR-001");
        product.setName("Dell Monitor");
        product.setPrice(new BigDecimal("200.00"));
        product.setActive(true);

        CreateOrderRequest request = new CreateOrderRequest(
                "Alexander",
                "alexander@example.com",
                new DeliveryAddressRequest(
                        "Bulgaria",
                        "Sofia",
                        "1000",
                        "Vitosha Boulevard",
                        "Apartment 7"
                ),
                List.of(new OrderItemRequest(productId, requestedQuantity))
        );

        when(productService.getActiveProduct(productId))
                .thenReturn(product);

        doThrow(new InsufficientStockException(
                productId,
                requestedQuantity,
                availableQuantity
        ))
                .when(inventoryService)
                .validateAvailableStock(productId, requestedQuantity);

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(request)
        );

        verify(productService).getActiveProduct(productId);

        verify(inventoryService)
                .validateAvailableStock(productId, requestedQuantity);

        verify(inventoryService, never())
                .reserveStock(any(ReserveStockRequest.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldNotSaveOrderWhenSecondReservationFails() {

        UUID monitorId = UUID.randomUUID();
        UUID keyboardId = UUID.randomUUID();

        Product monitor = new Product();
        monitor.setId(monitorId);
        monitor.setSku("MONITOR-001");
        monitor.setName("Dell Monitor");
        monitor.setPrice(new BigDecimal("200.00"));
        monitor.setActive(true);

        Product keyboard = new Product();
        keyboard.setId(keyboardId);
        keyboard.setSku("KEYBOARD-001");
        keyboard.setName("Mechanical Keyboard");
        keyboard.setPrice(new BigDecimal("80.00"));
        keyboard.setActive(true);

        CreateOrderRequest request = new CreateOrderRequest(
                "Alexander",
                "alexander@example.com",
                new DeliveryAddressRequest(
                        "Bulgaria",
                        "Sofia",
                        "1000",
                        "Vitosha Boulevard",
                        "Apartment 7"
                ),
                List.of(
                        new OrderItemRequest(monitorId, 2),
                        new OrderItemRequest(keyboardId, 1)
                )
        );

        when(productService.getActiveProduct(monitorId))
                .thenReturn(monitor);

        when(productService.getActiveProduct(keyboardId))
                .thenReturn(keyboard);

        doAnswer(invocation -> {
            ReserveStockRequest reservation = invocation.getArgument(0);

            if (reservation.productId().equals(keyboardId)) {
                throw new InsufficientStockException(
                        keyboardId,
                        1,
                        0
                );
            }

            return null;
        })
                .when(inventoryService)
                .reserveStock(any(ReserveStockRequest.class));

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(request)
        );

        verify(inventoryService)
                .validateAvailableStock(monitorId, 2);

        verify(inventoryService)
                .validateAvailableStock(keyboardId, 1);

        verify(inventoryService)
                .reserveStock(new ReserveStockRequest(monitorId, 2));

        verify(inventoryService)
                .reserveStock(new ReserveStockRequest(keyboardId, 1));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(orderId)
        );

        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldReturnOrderWhenOrderExists() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCustomerName("Alexander");
        order.setCustomerEmail("alexander@example.com");
        order.setDeliveryAddress(new DeliveryAddress(
                "Bulgaria",
                "Sofia",
                "1000",
                "Vitosha Boulevard",
                "Apartment 7"
        ));

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(orderId);

        assertEquals(orderId, response.orderId());
        assertEquals(OrderStatus.CONFIRMED, response.status());
        assertEquals("Alexander", response.customerName());
        assertEquals("alexander@example.com", response.customerEmail());
        assertEquals(BigDecimal.ZERO, response.totalPrice());

        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldThrowWhenCancellingMissingOrder() {

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(orderId));

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(inventoryService);
    }

    @Test
    void shouldThrowWhenCancellingOrderWithInvalidStatus() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.cancelOrder(orderId)
        );

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(inventoryService);

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldCancelConfirmedOrderSuccessfully() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setProductSku("MONITOR-001");
        orderItem.setProductName("Dell Monitor");
        orderItem.setUnitPrice(new BigDecimal("200.00"));

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCustomerName("Alexander");
        order.setCustomerEmail("alexander@example.com");
        order.addItem(orderItem);
        order.setDeliveryAddress(new DeliveryAddress(
                "Bulgaria",
                "Sofia",
                "1000",
                "Vitosha Boulevard",
                "Apartment 7"
        ));

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCELLED, response.status());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(inventoryService)
                .releaseReservedStock(
                        new ReleaseStockRequest(productId, 2)
                );

        verify(orderRepository).save(order);
    }

    @Test
    void shouldKeepOrderConfirmedWhenSecondReservationReleaseFails() {

        UUID orderId = UUID.randomUUID();
        UUID monitorId = UUID.randomUUID();
        UUID keyboardId = UUID.randomUUID();

        Product monitor = new Product();
        monitor.setId(monitorId);

        Product keyboard = new Product();
        keyboard.setId(keyboardId);

        OrderItem monitorItem = new OrderItem();
        monitorItem.setProduct(monitor);
        monitorItem.setQuantity(2);

        OrderItem keyboardItem = new OrderItem();
        keyboardItem.setProduct(keyboard);
        keyboardItem.setQuantity(1);

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.addItem(monitorItem);
        order.addItem(keyboardItem);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        doAnswer(invocation -> {

            ReleaseStockRequest request = invocation.getArgument(0);

            if (request.productId().equals(keyboardId)) {
                throw new InsufficientReservedStockException(
                        keyboardId,
                        1,
                        0
                );
            }

            return null;
        })
                .when(inventoryService)
                .releaseReservedStock(any(ReleaseStockRequest.class));

        assertThrows(
                InsufficientReservedStockException.class,
                () -> orderService.cancelOrder(orderId)
        );

        verify(inventoryService)
                .releaseReservedStock(
                        new ReleaseStockRequest(monitorId, 2)
                );

        verify(inventoryService)
                .releaseReservedStock(
                        new ReleaseStockRequest(keyboardId, 1)
                );

        verify(orderRepository, never())
                .save(any(Order.class));

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldThrowWhenShippingMissingOrder() {

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.shipOrder(orderId));

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(inventoryService);
    }

    @Test
    void shouldThrowWhenShippingOrderWithInvalidStatus() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.shipOrder(orderId)
        );

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(inventoryService);

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldShipConfirmedOrderSuccessfully() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setProductSku("MONITOR-001");
        orderItem.setProductName("Dell Monitor");
        orderItem.setUnitPrice(new BigDecimal("200.00"));

        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setCountry("Bulgaria");
        deliveryAddress.setCity("Sofia");
        deliveryAddress.setPostalCode("1000");
        deliveryAddress.setStreet("Vitosha Boulevard");
        deliveryAddress.setAddressLine("Apartment 7");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCustomerName("Alexander");
        order.setCustomerEmail("alexander@example.com");
        order.setDeliveryAddress(deliveryAddress);
        order.addItem(orderItem);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.shipOrder(orderId);

        assertEquals(OrderStatus.SHIPPED, response.status());
        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        verify(inventoryService)
                .shipStock(new ShipStockRequest(productId, 2));

        verify(orderRepository).save(order);
    }

    @Test
    void shouldKeepOrderConfirmedWhenSecondShipmentFails() {

        UUID orderId = UUID.randomUUID();
        UUID monitorId = UUID.randomUUID();
        UUID keyboardId = UUID.randomUUID();

        Product monitor = new Product();
        monitor.setId(monitorId);

        Product keyboard = new Product();
        keyboard.setId(keyboardId);

        OrderItem monitorItem = new OrderItem();
        monitorItem.setProduct(monitor);
        monitorItem.setQuantity(2);

        OrderItem keyboardItem = new OrderItem();
        keyboardItem.setProduct(keyboard);
        keyboardItem.setQuantity(1);

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.addItem(monitorItem);
        order.addItem(keyboardItem);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        doAnswer(invocation -> {

            ShipStockRequest request = invocation.getArgument(0);

            if (request.productId().equals(keyboardId)) {
                throw new InsufficientReservedStockException(
                        keyboardId,
                        1,
                        0
                );
            }

            return null;
        })
                .when(inventoryService)
                .shipStock(any(ShipStockRequest.class));

        assertThrows(
                InsufficientReservedStockException.class,
                () -> orderService.shipOrder(orderId)
        );

        verify(inventoryService)
                .shipStock(
                        new ShipStockRequest(monitorId, 2)
                );

        verify(inventoryService)
                .shipStock(
                        new ShipStockRequest(keyboardId, 1)
                );

        verify(orderRepository, never())
                .save(any(Order.class));

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }
}
