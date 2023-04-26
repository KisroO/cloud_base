package com.kisro.cloud.controller;

import com.kisro.cloud.service.impl.RecoverJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zoutao
 * @since 2023/3/9
 **/
@RequestMapping("/recover")
@RestController
public class RecoverController {
    @Autowired
    private RecoverJob recoverJob;

    @PostMapping("/sim")
    public String recover(@RequestParam(name = "sheet", defaultValue = "1", required = false) Integer sheet,
                          @RequestParam(name = "readRows", defaultValue = "1", required = false) Integer readRows) {
        recoverJob.exec(sheet, readRows);
        return "SUCCEED";
    }
}
