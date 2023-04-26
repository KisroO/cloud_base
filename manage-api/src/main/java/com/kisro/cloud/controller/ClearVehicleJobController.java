package com.kisro.cloud.controller;

import com.kisro.cloud.service.impl.ClearVehicleJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zoutao
 * @since 2023/2/15
 **/
@RestController
@RequestMapping("/job")
public class ClearVehicleJobController {
    @Autowired
    private ClearVehicleJob clearVehicleJob;

    @PostMapping("/clear")
    public String clear(@RequestParam(name = "sheet", defaultValue = "1", required = false) Integer sheet,
                        @RequestParam(name = "readRows", defaultValue = "1", required = false) Integer readRows) {
        clearVehicleJob.clear(sheet, readRows);
        return "SUCCEED";
    }

    @PostMapping("/init")
    public String init() {
        clearVehicleJob.init();
        return "SUCCEED";
    }

    @PostMapping("/test")
    public String test() {
        clearVehicleJob.test();
        return "SUCCEED";
    }

}
