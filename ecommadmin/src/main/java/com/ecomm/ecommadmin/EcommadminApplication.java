package com.ecomm.ecommadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EcommadminApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommadminApplication.class, args);
    }

}
