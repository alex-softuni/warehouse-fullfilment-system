package com.ft.warehousefullfilmentsystem.order.domain;

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
