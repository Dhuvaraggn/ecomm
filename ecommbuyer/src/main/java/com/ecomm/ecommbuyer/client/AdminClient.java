package com.ecomm.ecommbuyer.client;

import com.ecomm.ecommbuyer.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "admin-service", url = "${admin.service.url}")
public interface AdminClient {

    @GetMapping("/api/admin/products/{id}")
    ProductResponse getProductById(@PathVariable Long id);

    @GetMapping("/api/admin/products/available")
    List<ProductResponse> getAllAvailableProducts();
}
