package com.ft.warehousefullfilmentsystem.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySku(String sku);
    List<Product> findAllByActiveTrue();
    Optional<Product> findByIdAndActiveTrue(UUID id);
    List<Product> findAllByActiveFalse();
}
