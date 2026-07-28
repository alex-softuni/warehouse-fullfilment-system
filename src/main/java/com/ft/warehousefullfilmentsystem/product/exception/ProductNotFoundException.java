package com.ft.warehousefullfilmentsystem.product.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID id) {
        super("Product with id '" + id + "' was not found.");
    }}
