package com.ecomm.ecommbuyer.controller;

import com.ecomm.ecommbuyer.dto.ProductResponse;
import com.ecomm.ecommbuyer.entity.Cart;
import com.ecomm.ecommbuyer.entity.Order;
import com.ecomm.ecommbuyer.service.BuyerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BuyerControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private BuyerService buyerService;

    @Test
    void testBrowseProducts() {
        ProductResponse product = new ProductResponse(1L, 2L, "Test Product", "Description", BigDecimal.valueOf(100.0), 10);
        List<ProductResponse> products = Arrays.asList(product);

        when(buyerService.browseProducts()).thenReturn(products);

        ResponseEntity<List> result = restTemplate.getForEntity("/api/buyer/products", List.class);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void testAddToCart_Success() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setBuyerId(1L);
        cart.setProductId(1L);
        cart.setQuantity(2);

        when(buyerService.addToCart(1L, 2, "buyer")).thenReturn(cart);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Cart> result = restTemplate.postForEntity("/api/buyer/cart?productId=1&quantity=2", entity, Cart.class);

        assertEquals(201, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals(2, result.getBody().getQuantity());
    }

    @Test
    void testAddToCart_BadRequest() {
        when(buyerService.addToCart(1L, 2, "buyer")).thenThrow(new RuntimeException("Insufficient product quantity available"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.postForEntity("/api/buyer/cart?productId=1&quantity=2", entity, String.class);

        assertEquals(400, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("Insufficient product quantity available"));
    }

    @Test
    void testGetCart_Success() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setBuyerId(1L);
        cart.setProductId(1L);
        cart.setQuantity(2);
        List<Cart> cartItems = Arrays.asList(cart);

        when(buyerService.getCart("buyer")).thenReturn(cartItems);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> result = restTemplate.exchange("/api/buyer/cart", HttpMethod.GET, entity, List.class);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void testGetCart_Forbidden() {
        when(buyerService.getCart("buyer")).thenThrow(new RuntimeException("Access denied. Buyer role required."));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange("/api/buyer/cart", HttpMethod.GET, entity, String.class);

        assertEquals(403, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("Access denied. Buyer role required."));
    }

    @Test
    void testRemoveFromCart_Success() {
        doNothing().when(buyerService).removeFromCart(1L, "buyer");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange("/api/buyer/cart/1", HttpMethod.DELETE, entity, String.class);

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("Item removed from cart"));
    }

    @Test
    void testRemoveFromCart_BadRequest() {
        doThrow(new RuntimeException("Cart item not found")).when(buyerService).removeFromCart(1L, "buyer");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange("/api/buyer/cart/1", HttpMethod.DELETE, entity, String.class);

        assertEquals(400, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("Cart item not found"));
    }

    @Test
    void testPlaceOrder_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setBuyerId(1L);
        order.setProductId(1L);
        order.setQuantity(2);
        order.setTotalPrice(BigDecimal.valueOf(200.0));
        order.setStatus(Order.OrderStatus.PLACED);
        List<Order> orders = Arrays.asList(order);

        when(buyerService.placeOrder("buyer")).thenReturn(orders);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> result = restTemplate.exchange("/api/buyer/orders", HttpMethod.POST, entity, List.class);

        assertEquals(201, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void testPlaceOrder_BadRequest() {
        when(buyerService.placeOrder("buyer")).thenThrow(new RuntimeException("Cart is empty"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange("/api/buyer/orders", HttpMethod.POST, entity, String.class);

        assertEquals(400, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("Cart is empty"));
    }

    @Test
    void testGetOrderHistory_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setBuyerId(1L);
        order.setProductId(1L);
        order.setQuantity(2);
        order.setTotalPrice(BigDecimal.valueOf(200.0));
        order.setStatus(Order.OrderStatus.PLACED);
        List<Order> orders = Arrays.asList(order);

        when(buyerService.getOrderHistory("buyer")).thenReturn(orders);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> result = restTemplate.exchange("/api/buyer/orders", HttpMethod.GET, entity, List.class);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void testGetOrderHistory_Forbidden() {
        when(buyerService.getOrderHistory("buyer")).thenThrow(new RuntimeException("Access denied. Buyer role required."));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "buyer");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange("/api/buyer/orders", HttpMethod.GET, entity, String.class);

        assertEquals(403, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("Access denied. Buyer role required."));
    }
}