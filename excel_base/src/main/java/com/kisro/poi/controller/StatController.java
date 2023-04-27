package com.kisro.poi.controller;

import com.kisro.poi.payload.LossRateExport;
import com.kisro.poi.payload.LossRateResult;
import com.kisro.poi.service.LossRateService;
import com.nex.bu1.io.export.ExportEx;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.util.ListEx;
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
public class StatController {
    private final LossRateService lossRateService;

    @GetMapping("/singleStat")
    public void singleStat(@RequestParam("vin") String vin,
                           @RequestParam("date") Date date,
                           @RequestParam("exportFlag") boolean exportFlag,
                           HttpServletResponse response){
        LossRateResult data = lossRateService.singleCarStat(vin, date);
        if (exportFlag) {
            LossRateExport export = new LossRateExport();
            BeanUtils.copyProperties(data, export);
            export.setVin(vin);
            export.setDate(date);
            try {
                ExportEx.exportExcel(response, "丢包率统计", ListEx.newArrayList(export), LossRateExport.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(JsonEx.toJsonString(data));
        }
    }
}
