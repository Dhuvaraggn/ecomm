package com.ecomm.ecommbuyer.service;

import com.ecomm.ecommbuyer.dto.AuthResponse;
import com.ecomm.ecommbuyer.client.AdminClient;
import com.ecomm.ecommbuyer.client.AuthClient;
import com.ecomm.ecommbuyer.dto.ProductResponse;
import com.ecomm.ecommbuyer.entity.Cart;
import com.ecomm.ecommbuyer.entity.Order;
import com.ecomm.ecommbuyer.repository.CartRepository;
import com.ecomm.ecommbuyer.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuyerService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthClient authClient;
    private final AdminClient adminClient;

    public Cart addToCart(Long productId, Integer quantity, String authHeader) {
        // Validate JWT token
        AuthResponse authResponse = authClient.validateToken(authHeader);

        if (!"Token is valid".equals(authResponse.getMessage())) {
            throw new RuntimeException("Invalid or expired token");
        }

        if (!"BUYER".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Buyer role required.");
        }

        // Verify product exists and has sufficient quantity
        ProductResponse product = adminClient.getProductById(productId);
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient product quantity available");
        }

        // Check if product already in cart
        var existingCart = cartRepository.findByBuyerIdAndProductId(authResponse.getUserId(), productId);

        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            return cartRepository.save(cart);
        } else {
            Cart cart = new Cart();
            cart.setBuyerId(authResponse.getUserId());
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }
    }

    public List<Cart> getCart(String authHeader) {
        // Validate JWT token
        AuthResponse authResponse = authClient.validateToken(authHeader);

        if (!"Token is valid".equals(authResponse.getMessage())) {
            throw new RuntimeException("Invalid or expired token");
        }

        if (!"BUYER".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Buyer role required.");
        }

        return cartRepository.findByBuyerId(authResponse.getUserId());
    }

    public void removeFromCart(Long cartId, String authHeader) {
        // Validate JWT token
        AuthResponse authResponse = authClient.validateToken(authHeader);

        if (!"Token is valid".equals(authResponse.getMessage())) {
            throw new RuntimeException("Invalid or expired token");
        }

        if (!"BUYER".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Buyer role required.");
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Ensure buyer owns the cart item
        if (!cart.getBuyerId().equals(authResponse.getUserId())) {
            throw new RuntimeException("Access denied. You can only remove your own cart items.");
        }

        cartRepository.delete(cart);
    }

    @Transactional
    public List<Order> placeOrder(String authHeader) {
        // Validate JWT token
        AuthResponse authResponse = authClient.validateToken(authHeader);

        if (!"Token is valid".equals(authResponse.getMessage())) {
            throw new RuntimeException("Invalid or expired token");
        }

        if (!"BUYER".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Buyer role required.");
        }

        List<Cart> cartItems = cartRepository.findByBuyerId(authResponse.getUserId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create orders from cart items
        List<Order> orders = cartItems.stream().map(cartItem -> {
            ProductResponse product = adminClient.getProductById(cartItem.getProductId());

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient quantity for product: " + product.getName());
            }

            Order order = new Order();
            order.setBuyerId(authResponse.getUserId());
            order.setProductId(cartItem.getProductId());
            order.setQuantity(cartItem.getQuantity());
            order.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.setStatus(Order.OrderStatus.PLACED);

            return orderRepository.save(order);
        }).toList();

        // Clear cart after placing order
        cartRepository.deleteByBuyerId(authResponse.getUserId());

        return orders;
    }

    public List<Order> getOrderHistory(String authHeader) {
        // Validate JWT token
        AuthResponse authResponse = authClient.validateToken(authHeader);

        if (!"Token is valid".equals(authResponse.getMessage())) {
            throw new RuntimeException("Invalid or expired token");
        }

        if (!"BUYER".equals(authResponse.getRole())) {
            throw new RuntimeException("Access denied. Buyer role required.");
        }

        return orderRepository.findByBuyerIdOrderByOrderDateDesc(authResponse.getUserId());
    }

    public List<ProductResponse> browseProducts() {
        return adminClient.getAllAvailableProducts();
    }
}
