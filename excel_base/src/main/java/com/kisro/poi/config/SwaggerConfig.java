package com.kisro.poi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author zoutao
 * @date 2023/4/28
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("车辆法规相关API").description("车辆法规。\n")
                .version("1.0")
                .build();
    }

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .enable(true)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.kisro.poi.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
