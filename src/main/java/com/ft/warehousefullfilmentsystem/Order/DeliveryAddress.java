package com.ft.warehousefullfilmentsystem.Order;

import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class DeliveryAddress {
    private String country;
    private String city;
    private String postalCode;
    private String street;
    private String addressLine;
}
