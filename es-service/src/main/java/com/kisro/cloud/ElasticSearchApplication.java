package com.kisro.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.kisro.cloud.dao")
public class ElasticSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElasticSearchApplication.class, args);
    }
}
