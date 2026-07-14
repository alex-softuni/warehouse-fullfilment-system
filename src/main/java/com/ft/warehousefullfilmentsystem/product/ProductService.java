package com.ft.warehousefullfilmentsystem.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getSku(),
                savedProduct.getName(),
                savedProduct.getPrice()
        );
    }
}
