package com.kisro.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author Kisro
 * @since 2022/11/1
 **/
@SpringBootApplication
@EnableElasticsearchRepositories
public class EsBaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(EsBaseApplication.class, args);
    }
}
