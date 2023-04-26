package com.kisro.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.kisro.cloud.pojo.Report;
import com.kisro.cloud.pojo.Vehicle;
import com.kisro.cloud.service.IReportService;
import com.kisro.cloud.service.IVehicleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@RestController
@Api(tags = "报表接口")
@RequestMapping("/report")
public class ReportController {
    @Resource
    private IReportService reportService;
    @Resource
    private IVehicleService vehicleService;

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

    @PostMapping("/batchInsert")
    public void batchInsert() {
        List<Report> list = new ArrayList<>(10000);
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 10000; j++) {
                list.add(new Report("cs", i * 10000 + j + "", "content cs"));
            }
            reportService.saveBatch(list);
            list = new ArrayList<>(10000);
        }
    }

    @PostMapping("/batchInsertPojo")
    public void batchInsertPojo() {
        List<Vehicle> list = new ArrayList<>(10000);
        for (int i = 0; i < 18; i++) {
            for (int j = 0; j < 10000; j++) {
                list.add(new Vehicle(i * 10000 + j + "", "content cs"));
            }
            vehicleService.saveBatch(list);
            list = new ArrayList<>(10000);
        }
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
