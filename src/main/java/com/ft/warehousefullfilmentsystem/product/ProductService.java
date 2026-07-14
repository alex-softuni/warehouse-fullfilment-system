package com.ft.warehousefullfilmentsystem.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

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

        return toResponse(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return toResponse(product);
    }

    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name().trim());
        product.setPrice(request.price());

        Product updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
    }
    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice()
        );
    }
}
