package com.kisro.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.kisro.cloud.pojo.Report;
import com.kisro.cloud.service.IReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@RestController
@Api(tags = "报表接口")
@RequestMapping("/report")
public class ReportController {
    @Autowired
    private IReportService reportService;

    @GetMapping("/info/{id}")
    @ApiOperation(value = "查询报表", notes = "根据id查询报表")
    public String info(@PathVariable("id") Long id) {
        Report report = reportService.reportInfo(id);
        return JSONObject.toJSONString(report);
    }

    @PostMapping("/insert")
    @ApiOperation("插入报表")
    public String insert(@RequestBody Report report) {
        long id = reportService.insert(report);
        return "success, id: " + id;
    }

    @PutMapping("/update")
    @ApiOperation("更新报表")
    public String update(@RequestBody Report report) {
        reportService.updateReport(report);
        return "updated";
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除报表")
    public String delete(@PathVariable("id") Long id) {
        reportService.deleteReport(id);
        return "success";
    }
}
