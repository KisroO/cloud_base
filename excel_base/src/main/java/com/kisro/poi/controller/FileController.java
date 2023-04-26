package com.kisro.poi.controller;

import com.kisro.poi.payload.LossRateExport;
import com.kisro.poi.payload.LossRateResult;
import com.kisro.poi.service.FileService;
import com.kisro.poi.service.MessageReportService;
import com.kisro.poi.service.MessageService;
import com.nex.bu1.io.export.ExportEx;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.util.ListEx;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author zoutao
 * @since 2023/4/6
 **/
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final MessageService messageService;
    private final MessageReportService messageReportService;

    @PostMapping("/import")
    public String importFile(MultipartFile file) {
        fileService.importFile(file);
        return "success";
    }

    @PostMapping("/importVehicle")
    public String importVehicle(MultipartFile file) {
        return fileService.importVehicleData(file);
    }

    @GetMapping("/lossRate")
    public String lossRate(@RequestParam("vin") String vin) {
        return messageService.statPacketLossRate(vin);
    }

    /**
     * 单台车正常数据丢包率统计
     *
     * @param vin
     * @return
     */
    @GetMapping("/normalStat")
    public String normalStat(@RequestParam("vin") String vin,
                             @RequestParam("date") Date date) {
        return JsonEx.toJsonString(messageService.normalStat(vin, date));
    }

    /**
     * 多台车正常数据丢包率统计
     */
    @GetMapping("/multiNormalStat")
    public void multiNormalStat(@RequestParam("vin") List<String> vinList,
                                @RequestParam("startDate") Date startDate,
                                @RequestParam("endDate") Date endDate,
                                @RequestParam("exportFlag") boolean exportFlag,
                                HttpServletResponse response) throws IOException {
        List<LossRateExport> dataList = messageService.multiVehicleStat(vinList, startDate, endDate);
        if (exportFlag) {
            ExportEx.exportExcel(response, "丢包率统计", dataList, LossRateExport.class);
        } else {
            System.out.println(JsonEx.toJsonString(dataList));
        }
    }

    @GetMapping("/newStat")
    public void newStat(@RequestParam("vin") String vin,
                        @RequestParam("date") Date date,
                        @RequestParam("exportFlag") boolean exportFlag,
                        HttpServletResponse response) throws IOException {
        LossRateResult data = messageReportService.newStat(vin, date);
        if (exportFlag) {
            LossRateExport export = new LossRateExport();
            BeanUtils.copyProperties(data, export);
            export.setVin(vin);
            export.setDate(date);
            ExportEx.exportExcel(response, "丢包率统计", ListEx.newArrayList(export), LossRateExport.class);
        } else {
            System.out.println(JsonEx.toJsonString(data));
        }
    }

    @GetMapping("/newMultiStat")
    public void newMultiStat(@RequestParam("vin") List<String> vin,
                             @RequestParam("startDate") Date startDate,
                             @RequestParam("endDate") Date endDate,
                             @RequestParam("exportFlag") boolean exportFlag,
                             HttpServletResponse response) throws IOException {
        List<LossRateExport> dataList = messageReportService.newMultiStat(vin, startDate, endDate);
        if (exportFlag) {
            ExportEx.exportExcel(response, "丢包率统计", ListEx.newArrayList(dataList), LossRateExport.class);
        } else {
            System.out.println(JsonEx.toJsonString(dataList));
        }
    }

    @GetMapping("/newStat/v2")
    public void newStatV2(@RequestParam("vin") String vin,
                          @RequestParam("date") Date date,
                          @RequestParam("exportFlag") boolean exportFlag,
                          HttpServletResponse response) throws IOException {
        LossRateResult data = messageReportService.newStatV2(vin, date);
        if (exportFlag) {
            LossRateExport export = new LossRateExport();
            BeanUtils.copyProperties(data, export);
            export.setVin(vin);
            export.setDate(date);
            ExportEx.exportExcel(response, "丢包率统计", ListEx.newArrayList(export), LossRateExport.class);
        } else {
            System.out.println(JsonEx.toJsonString(data));
        }
    }

    //    @GetMapping("/lossRateStat/v2/multi")
//    public void newStatV2(@RequestParam("vin") List<String> vin,
//                          @RequestParam("startDate") Date startDate,
//                          @RequestParam("endDate") Date endDate,
//                          @RequestParam("exportFlag")boolean exportFlag,
//                          HttpServletResponse response) throws IOException {
//        LossRateResult data = messageReportService.newStatV2(vin, date);
//        if(exportFlag){
//            LossRateExport export = new LossRateExport();
//            BeanUtils.copyProperties(data,export);
//            export.setVin(vin);
//            export.setDate(date);
//            ExportEx.exportExcel(response, "丢包率统计", ListEx.newArrayList(export), LossRateExport.class);
//        }else {
//            System.out.println(JsonEx.toJsonString(data));
//        }
//    }
    @GetMapping("/transform")
    public void transform() throws Exception {
        fileService.transformFiles();
    }
}
