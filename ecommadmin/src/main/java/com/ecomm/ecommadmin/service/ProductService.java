package com.ecomm.ecommadmin.service;

import com.ecomm.ecommadmin.client.AuthClient;
import com.ecomm.ecommadmin.dto.AuthResponse;
import com.ecomm.ecommadmin.entity.Product;
import com.ecomm.ecommadmin.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final AuthClient authClient;

    public Product addProduct(Product product, String username) {
        AuthResponse authResponse = authClient.validateUser(username);
        if (!"ADMIN".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Admin role required.");
        }

        product.setSellerId(authResponse.getUserId());
        return productRepository.save(product);
    }


    public Product updateProduct(Long id, Product productDetails, String username) {
        AuthResponse authResponse = authClient.validateUser(username);
        if (!"ADMIN".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Admin role required.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Ensure seller owns the product
        if (!product.getSellerId().equals(authResponse.getUserId())) {
            throw new RuntimeException("Access denied. You can only update your own products.");
        }

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());

        return productRepository.save(product);
    }

    public List<Product> getSellerProducts(String username) {
        // Validate seller user
        AuthResponse authResponse = authClient.validateUser(username);
        if (!"ADMIN".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Seller role required.");
        }

        return productRepository.findBySellerId(authResponse.getUserId());
    }

    public Product updateQuantity(Long id, Integer quantity, String username) {
        // Validate seller user
        AuthResponse authResponse = authClient.validateUser(username);
        if (!"ADMIN".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Seller role required.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Ensure seller owns the product
        if (!product.getSellerId().equals(authResponse.getUserId())) {
            throw new RuntimeException("Access denied. You can only update your own products.");
        }

        product.setQuantity(quantity);
        return productRepository.save(product);
    }

    // Public method for other services to fetch product details
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> getAllAvailableProducts() {
        return productRepository.findByQuantityGreaterThan(0);
    }
}
