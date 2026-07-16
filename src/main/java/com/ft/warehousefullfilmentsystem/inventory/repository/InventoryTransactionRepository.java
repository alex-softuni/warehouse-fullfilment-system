package com.ft.warehousefullfilmentsystem.inventory.repository;

import com.ft.warehousefullfilmentsystem.inventory.domain.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, UUID> {

    List<InventoryTransaction> findAllByProductIdOrderByCreatedAtDesc(
            UUID productId
    );
}
