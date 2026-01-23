package com.ecomm.ecommadmin.controller;

import com.ecomm.ecommadmin.entity.Product;
import com.ecomm.ecommadmin.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddProduct_Success() throws Exception {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setQuantity(10);

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Test Product");

        when(productService.addProduct(any(Product.class), eq("seller"))).thenReturn(savedProduct);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "seller")
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void testAddProduct_AccessDenied() throws Exception {
        Product product = new Product();
        product.setName("Test Product");

        when(productService.addProduct(any(Product.class), eq("buyer"))).thenThrow(new RuntimeException("Access denied. Admin role required."));

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "buyer")
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied. Admin role required."));
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        Product product = new Product();
        product.setName("Updated Product");

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");

        when(productService.updateProduct(eq(1L), any(Product.class), eq("seller"))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "seller")
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    void testUpdateProduct_BadRequest() throws Exception {
        Product product = new Product();
        product.setName("Updated Product");

        when(productService.updateProduct(eq(1L), any(Product.class), eq("seller"))).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "seller")
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product not found"));
    }

    @Test
    void testGetSellerProducts_Success() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        List<Product> products = Arrays.asList(product);

        when(productService.getSellerProducts("seller")).thenReturn(products);

        mockMvc.perform(get("/api/admin/products")
                .header("Authorization", "seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void testUpdateQuantity_Success() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setQuantity(20);

        when(productService.updateQuantity(1L, 20, "seller")).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/admin/products/1/quantity")
                .param("quantity", "20")
                .header("Authorization", "seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    void testGetProductById_Success() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/admin/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(1L)).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/admin/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found"));
    }

    @Test
    void testGetAllAvailableProducts() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        List<Product> products = Arrays.asList(product);

        when(productService.getAllAvailableProducts()).thenReturn(products);

        mockMvc.perform(get("/api/admin/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }
}