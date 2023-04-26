package com.kisro.cloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author Kisro
 * @since 2022/11/1
 **/
@SpringBootApplication
@EnableElasticsearchRepositories
@MapperScan(basePackages = "com.kisro.cloud.dao")
public class EsBaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(EsBaseApplication.class, args);
    }
}
