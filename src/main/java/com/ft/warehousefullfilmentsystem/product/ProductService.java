package com.ft.warehousefullfilmentsystem.product;

import com.ft.warehousefullfilmentsystem.inventory.Inventory;
import com.ft.warehousefullfilmentsystem.inventory.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        String normalizedSku = request.sku()
                .trim()
                .toUpperCase();

        if (productRepository.existsBySku(normalizedSku)) {
            throw new DuplicateSkuException(normalizedSku);
        }

        Product product = new Product();

        product.setSku(request.sku().trim().toUpperCase());
        product.setName(request.name().trim());
        product.setPrice(request.price());

        Product savedProduct = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(savedProduct);
        inventory.setAvailableQuantity(0);
        inventory.setReservedQuantity(0);

        inventoryRepository.save(inventory);

        return toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name().trim());
        product.setPrice(request.price());

        Product updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
    }

    @Transactional
    public ProductResponse archiveProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (!product.isActive()) {
            return toResponse(product);
        }

        product.setActive(false);

        Product archivedProduct = productRepository.save(product);

        return toResponse(archivedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getArchivedProducts() {
        return productRepository.findAllByActiveFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse restoreProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (product.isActive()) {
            return toResponse(product);
        }

        product.setActive(true);

        Product restoredProduct = productRepository.save(product);

        return toResponse(restoredProduct);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.isActive()
        );
    }
}
