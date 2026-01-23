package com.ecomm.ecommadmin.service;

import com.ecomm.ecommadmin.client.AuthClient;
import com.ecomm.ecommadmin.dto.AuthResponse;
import com.ecomm.ecommadmin.entity.Product;
import com.ecomm.ecommadmin.repository.ProductRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSellerId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setQuantity(10);

        authResponse = new AuthResponse(1L, "seller", "ADMIN", "Validated", "Token is valid");
    }

    @Test
    void testAddProduct_Success() {
        when(authClient.validateToken("seller")).thenReturn(authResponse);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.addProduct(product, "seller");

        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testAddProduct_AccessDenied() {
        AuthResponse buyerResponse = new AuthResponse(2L, "buyer", "BUYER", "Validated", "Token is valid");
        when(authClient.validateToken("buyer")).thenReturn(buyerResponse);

        assertThatThrownBy(() -> productService.addProduct(product, "buyer"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Access denied. Admin role required.");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(BigDecimal.valueOf(150.0));
        updatedProduct.setQuantity(15);

        when(authClient.validateToken("seller")).thenReturn(authResponse);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.updateProduct(1L, updatedProduct, "seller");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Product");
        verify(productRepository).save(product);
    }

    @Test
    void testUpdateProduct_NotFound() {
        when(authClient.validateToken("seller")).thenReturn(authResponse);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(1L, product, "seller"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Product not found");
    }

    @Test
    void testUpdateProduct_AccessDenied() {
        AuthResponse otherSeller = new AuthResponse(2L, "other", "ADMIN", "Validated", "Token is valid");
        when(authClient.validateToken("other")).thenReturn(otherSeller);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProduct(1L, product, "other"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Access denied. You can only update your own products.");
    }

    @Test
    void testGetSellerProducts_Success() {
        List<Product> products = Arrays.asList(product);
        when(authClient.validateToken("seller")).thenReturn(authResponse);
        when(productRepository.findBySellerId(1L)).thenReturn(products);

        List<Product> result = productService.getSellerProducts("seller");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    void testUpdateQuantity_Success() {
        when(authClient.validateToken("seller")).thenReturn(authResponse);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.updateQuantity(1L, 20, "seller");

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(20);
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Product not found");
    }

    @Test
    void testGetAllAvailableProducts() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByQuantityGreaterThan(0)).thenReturn(products);

        List<Product> result = productService.getAllAvailableProducts();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }
}