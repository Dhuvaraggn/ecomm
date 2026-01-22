package com.ecomm.ecommadmin.repository;

import com.ecomm.ecommadmin.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByQuantityGreaterThan(Integer quantity);
}
