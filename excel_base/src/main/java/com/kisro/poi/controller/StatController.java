package com.kisro.poi.controller;

import com.kisro.poi.payload.LossRateExport;
import com.kisro.poi.payload.LossRateResult;
import com.kisro.poi.payload.LossRateResultExp;
import com.kisro.poi.service.LossRateService;
import com.nex.bu1.io.export.ExportEx;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.util.ListEx;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author zoutao
 * @date 2023/4/27
 */
@RestController
@RequestMapping("/stat")
@RequiredArgsConstructor
@Api(value = "统计Controller")
public class StatController {
    private final LossRateService lossRateService;

    @GetMapping("/singleStat")
    public void singleStat(@RequestParam("vin") String vin,
                           @RequestParam("date") Date date,
                           @RequestParam("exportFlag") boolean exportFlag,
                           HttpServletResponse response) {
        LossRateExport data = lossRateService.singleCarStat(vin, date);
        if (exportFlag) {
            try {
                ExportEx.exportExcel(response, "丢包率统计", ListEx.newArrayList(data), LossRateExport.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(JsonEx.toJsonString(data));
        }
    }

    @ApiOperation(value = "单车统计v2")
    @GetMapping("/singleStat/v2")
    public void singleStatInfo(@RequestParam("vin") String vin,
                               @RequestParam("date") Date date,
                               @RequestParam(value = "exportFlag", required = false, defaultValue = "false") boolean exportFlag,
                               @RequestParam(value = "exportDetailFlag", required = false, defaultValue = "false") boolean exportDetailFlag,
                               HttpServletResponse response) {
        if (exportFlag) {
            try {
                lossRateService.exportSingleStat(vin,date,exportDetailFlag,response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(JsonEx.toJsonString(lossRateService.singleCarStat(vin,date)));
        }
    }

    /**
     * 批量统计
     * @param vin
     * @param startDate
     * @param exportFlag
     * @param exportDetailFlag
     * @param response
     */
    public void multiStat(@RequestParam("vin") String vin,
                          @RequestParam("startDate") Date startDate,
                          @RequestParam(value = "exportFlag", required = false, defaultValue = "false") boolean exportFlag,
                          @RequestParam(value = "exportDetailFlag", required = false, defaultValue = "false") boolean exportDetailFlag,
                          HttpServletResponse response) {
    }
}
