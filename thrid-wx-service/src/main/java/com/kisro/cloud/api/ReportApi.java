package com.kisro.cloud.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@Component
@FeignClient(value = "manage-api")
public interface ReportApi {
    @GetMapping("/report/info/{id}")
    String reportById(@PathVariable("id") Long id);
}
