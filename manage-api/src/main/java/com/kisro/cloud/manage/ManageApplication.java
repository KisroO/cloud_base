package com.kisro.cloud.manage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(basePackages = "com.kisro.cloud.manage.mapper")
public class ManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageApplication.class, args);
    }
}
