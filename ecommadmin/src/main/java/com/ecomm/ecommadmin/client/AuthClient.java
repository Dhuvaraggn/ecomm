package com.ecomm.ecommadmin.client;

import com.ecomm.ecommadmin.dto.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthClient {

    @GetMapping("/api/auth/validate/{username}")
    AuthResponse validateUser(@PathVariable String username);
}
