package com.ft.warehousefullfilmentsystem.inventory;

import com.ft.warehousefullfilmentsystem.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InventoryTransactionType type;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime createdAt;


}

