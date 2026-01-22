package com.ecomm.ecommbuyer.repository;

import com.ecomm.ecommbuyer.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByBuyerId(Long buyerId);
    Optional<Cart> findByBuyerIdAndProductId(Long buyerId, Long productId);
    void deleteByBuyerId(Long buyerId);
}
