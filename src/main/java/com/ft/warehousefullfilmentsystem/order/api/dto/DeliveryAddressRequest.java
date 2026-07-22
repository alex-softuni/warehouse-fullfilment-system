package com.ft.warehousefullfilmentsystem.order.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryAddressRequest(

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Postal code is required")
        String postalCode,

        @NotBlank(message = "Street is required")
        String street,

        String addressLine
) {
}
