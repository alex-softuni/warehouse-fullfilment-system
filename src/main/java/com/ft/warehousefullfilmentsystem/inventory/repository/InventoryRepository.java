package com.ft.warehousefullfilmentsystem.inventory.repository;

import com.ft.warehousefullfilmentsystem.inventory.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findByProductId(UUID productId);
}
