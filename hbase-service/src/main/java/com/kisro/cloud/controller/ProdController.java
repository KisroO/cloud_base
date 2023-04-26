package com.kisro.cloud.controller;

import com.kisro.cloud.pojo.OriginalMessageRecordExp;
import com.kisro.cloud.service.MessageService;
import com.nex.bu1.io.export.ExportEx;
import com.nex.bu1.util.DateEx;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author zoutao
 * @since 2023/3/29
 **/
@RestController
@RequiredArgsConstructor
public class ProdController {
    private final MessageService messageService;

    @GetMapping("/v1/query/original")
    public void queryOriginal(HttpServletResponse response,
                              @RequestParam(value = "vin", required = false) String vin,
                              @RequestParam(value = "commandIds", required = false) String commandIds,
                              @RequestParam(value = "startTime", required = false) Long startTime,
                              @RequestParam(value = "endTime", required = false) Long endTime) throws IOException {
        List<OriginalMessageRecordExp> data = messageService.queryOriginal(vin, commandIds, startTime, endTime);
        String fn = "报文数据" + DateEx.nowStr(DateEx.FMT_YMD_HMS2);
        ExportEx.exportCsv(response, fn, data, OriginalMessageRecordExp.class);
    }
}
