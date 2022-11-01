package com.kisro.cloud.manage.controller;

import com.kisro.cloud.api.ReportApi;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@RestController
@RequestMapping("/info")
@AllArgsConstructor
public class InfoController {
    private ReportApi reportApi;

    @GetMapping("/{id}")
    public String infoById(@PathVariable("id") Long id) {
        String report = reportApi.reportById(id);
        return "remote: " + report;
    }
}
