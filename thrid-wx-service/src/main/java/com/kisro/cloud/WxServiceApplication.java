package com.kisro.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class WxServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WxServiceApplication.class, args);
    }
}
