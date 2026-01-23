package com.ecomm.ecommbuyer.client;

import com.ecomm.ecommbuyer.dto.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthClient {

    @GetMapping("/api/auth/validate-token")
    AuthResponse validateToken(@RequestHeader("Authorization") String authorizationHeader);
}
