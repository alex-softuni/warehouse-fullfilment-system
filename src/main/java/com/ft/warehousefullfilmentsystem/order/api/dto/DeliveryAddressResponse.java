package com.ft.warehousefullfilmentsystem.order.api.dto;

public record DeliveryAddressResponse(
        String country,
        String city,
        String postalCode,
        String street,
        String addressLine
) {
}
