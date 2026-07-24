package com.ft.warehousefullfilmentsystem.common.exception;

import com.ft.warehousefullfilmentsystem.inventory.exception.InsufficientReservedStockException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InsufficientStockException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InventoryNotFoundException;
import com.ft.warehousefullfilmentsystem.inventory.exception.InventoryOverflowException;
import com.ft.warehousefullfilmentsystem.order.exception.DuplicateOrderItemException;
import com.ft.warehousefullfilmentsystem.order.exception.InvalidOrderStatusException;
import com.ft.warehousefullfilmentsystem.order.exception.OrderNotFoundException;
import com.ft.warehousefullfilmentsystem.product.DuplicateSkuException;
import com.ft.warehousefullfilmentsystem.product.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleProductNotFound(ProductNotFoundException exception) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    @ExceptionHandler(DuplicateSkuException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateSku(DuplicateSkuException exception) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> validationErrors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (firstMessage, secondMessage) -> firstMessage
                ));

        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed.",
                LocalDateTime.now(),
                validationErrors
        );
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleInventoryNotFound(
            InventoryNotFoundException exception
    ) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    @ExceptionHandler(InventoryOverflowException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInventoryOverflow(
            InventoryOverflowException exception
    ) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInsufficientStock(
            InsufficientStockException exception
    ) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    @ExceptionHandler(InsufficientReservedStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInsufficientReservedStock(
            InsufficientReservedStockException exception
    ) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    @ExceptionHandler(DuplicateOrderItemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDuplicateOrderItem(
            DuplicateOrderItemException exception
    ) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleOrderNotFound(
            OrderNotFoundException exception
    ) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInvalidOrderStatus(
            InvalidOrderStatusException exception
    ) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                Map.of()
        );
    }
}
