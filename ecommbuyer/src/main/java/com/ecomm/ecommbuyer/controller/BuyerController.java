package com.ecomm.ecommbuyer.controller;

import com.ecomm.ecommbuyer.dto.ProductResponse;
import com.ecomm.ecommbuyer.entity.Cart;
import com.ecomm.ecommbuyer.entity.Order;
import com.ecomm.ecommbuyer.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buyer")
public class BuyerController {

    private final BuyerService buyerService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> browseProducts() {
        List<ProductResponse> products = buyerService.browseProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/cart")
    public ResponseEntity<?> addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Cart cart = buyerService.addToCart(productId, quantity, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/cart")
    public ResponseEntity<?> getCart(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Cart> cart = buyerService.getCart(authHeader);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/cart/{id}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            buyerService.removeFromCart(id, authHeader);
            return ResponseEntity.ok("Item removed from cart");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Order> orders = buyerService.placeOrder(authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrderHistory(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Order> orders = buyerService.getOrderHistory(authHeader);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
