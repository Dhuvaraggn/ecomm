package com.ecomm.ecommadmin.controller;

import com.ecomm.ecommadmin.entity.Product;
import com.ecomm.ecommadmin.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(
            @RequestBody Product product,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Product created = productService.addProduct(product, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Product updated = productService.updateProduct(id, product, authHeader);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getSellerProducts(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Product> products = productService.getSellerProducts(authHeader);
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/products/{id}/quantity")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Product updated = productService.updateQuantity(id, quantity, authHeader);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Public endpoints for other services
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/products/available")
    public ResponseEntity<List<Product>> getAllAvailableProducts() {
        List<Product> products = productService.getAllAvailableProducts();
        return ResponseEntity.ok(products);
    }
}
