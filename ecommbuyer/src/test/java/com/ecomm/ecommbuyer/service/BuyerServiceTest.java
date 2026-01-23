package com.ecomm.ecommbuyer.service;

import com.ecomm.ecommbuyer.dto.AuthResponse;
import com.ecomm.ecommbuyer.client.AdminClient;
import com.ecomm.ecommbuyer.client.AuthClient;
import com.ecomm.ecommbuyer.dto.ProductResponse;
import com.ecomm.ecommbuyer.entity.Cart;
import com.ecomm.ecommbuyer.entity.Order;
import com.ecomm.ecommbuyer.repository.CartRepository;
import com.ecomm.ecommbuyer.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyerServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuthClient authClient;

    @Mock
    private AdminClient adminClient;

    @InjectMocks
    private BuyerService buyerService;

    private AuthResponse authResponse;
    private ProductResponse productResponse;
    private Cart cart;
    private Order order;

    @BeforeEach
    void setUp() {
        authResponse = new AuthResponse(1L, "buyer", "BUYER", "Validated", "Token is valid");

        productResponse = new ProductResponse(1L, 2L, "Test Product", "Description", BigDecimal.valueOf(100.0), 10);

        cart = new Cart();
        cart.setId(1L);
        cart.setBuyerId(1L);
        cart.setProductId(1L);
        cart.setQuantity(2);

        order = new Order();
        order.setId(1L);
        order.setBuyerId(1L);
        order.setProductId(1L);
        order.setQuantity(2);
        order.setTotalPrice(BigDecimal.valueOf(200.0));
        order.setStatus(Order.OrderStatus.PLACED);
    }

    @Test
    void testAddToCart_NewItem() {
        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(adminClient.getProductById(1L)).thenReturn(productResponse);
        when(cartRepository.findByBuyerIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = buyerService.addToCart(1L, 2, "buyer");

        assertNotNull(result);
        assertEquals(1L, result.getBuyerId());
        assertEquals(1L, result.getProductId());
        assertEquals(2, result.getQuantity());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testAddToCart_ExistingItem() {
        Cart existingCart = new Cart();
        existingCart.setId(1L);
        existingCart.setBuyerId(1L);
        existingCart.setProductId(1L);
        existingCart.setQuantity(1);

        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(adminClient.getProductById(1L)).thenReturn(productResponse);
        when(cartRepository.findByBuyerIdAndProductId(1L, 1L)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        Cart result = buyerService.addToCart(1L, 2, "buyer");

        assertNotNull(result);
        assertEquals(3, result.getQuantity()); // 1 + 2
        verify(cartRepository).save(existingCart);
    }

    @Test
    void testAddToCart_InsufficientQuantity() {
        ProductResponse lowStockProduct = new ProductResponse(1L, 2L, "Test Product", "Description", BigDecimal.valueOf(100.0), 1);

        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(adminClient.getProductById(1L)).thenReturn(lowStockProduct);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> buyerService.addToCart(1L, 5, "buyer"));
        assertEquals("Insufficient product quantity available", exception.getMessage());
    }

    @Test
    void testAddToCart_AccessDenied() {
        AuthResponse adminResponse = new AuthResponse(1L, "admin", "ADMIN", "Validated", "Token is valid");
        when(authClient.validateToken("admin")).thenReturn(adminResponse);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> buyerService.addToCart(1L, 2, "admin"));
        assertEquals("Access denied. Buyer role required.", exception.getMessage());
    }

    @Test
    void testGetCart_Success() {
        List<Cart> cartItems = Arrays.asList(cart);
        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(cartRepository.findByBuyerId(1L)).thenReturn(cartItems);

        List<Cart> result = buyerService.getCart("buyer");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBuyerId());
    }

    @Test
    void testRemoveFromCart_Success() {
        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> buyerService.removeFromCart(1L, "buyer"));
        verify(cartRepository).delete(cart);
    }

    @Test
    void testRemoveFromCart_NotFound() {
        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> buyerService.removeFromCart(1L, "buyer"));
        assertEquals("Cart item not found", exception.getMessage());
    }

    @Test
    void testRemoveFromCart_AccessDenied() {
        AuthResponse otherBuyer = new AuthResponse(2L, "other", "BUYER", "Validated", "Token is valid");
        when(authClient.validateToken("other")).thenReturn(otherBuyer);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> buyerService.removeFromCart(1L, "other"));
        assertEquals("Access denied. You can only remove your own cart items.", exception.getMessage());
    }

    @Test
    void testPlaceOrder_Success() {
        List<Cart> cartItems = Arrays.asList(cart);
        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(cartRepository.findByBuyerId(1L)).thenReturn(cartItems);
        when(adminClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        List<Order> result = buyerService.placeOrder("buyer");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Order.OrderStatus.PLACED, result.get(0).getStatus());
        verify(cartRepository).deleteByBuyerId(1L);
    }

    @Test
    void testPlaceOrder_EmptyCart() {
        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(cartRepository.findByBuyerId(1L)).thenReturn(Arrays.asList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> buyerService.placeOrder("buyer"));
        assertEquals("Cart is empty", exception.getMessage());
    }

    @Test
    void testPlaceOrder_InsufficientQuantity() {
        ProductResponse lowStockProduct = new ProductResponse(1L, 2L, "Test Product", "Description", BigDecimal.valueOf(100.0), 1);
        List<Cart> cartItems = Arrays.asList(cart); // cart has quantity 2

        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(cartRepository.findByBuyerId(1L)).thenReturn(cartItems);
        when(adminClient.getProductById(1L)).thenReturn(lowStockProduct);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> buyerService.placeOrder("buyer"));
        assertEquals("Insufficient quantity for product: Test Product", exception.getMessage());
    }

    @Test
    void testGetOrderHistory_Success() {
        List<Order> orders = Arrays.asList(order);
        when(authClient.validateToken("buyer")).thenReturn(authResponse);
        when(orderRepository.findByBuyerIdOrderByOrderDateDesc(1L)).thenReturn(orders);

        List<Order> result = buyerService.getOrderHistory("buyer");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBuyerId());
    }

    @Test
    void testBrowseProducts() {
        List<ProductResponse> products = Arrays.asList(productResponse);
        when(adminClient.getAllAvailableProducts()).thenReturn(products);

        List<ProductResponse> result = buyerService.browseProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
    }
}