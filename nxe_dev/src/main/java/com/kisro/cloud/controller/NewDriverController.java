package com.kisro.cloud.controller;

import com.kisro.cloud.dao.VehicleRefuelingRecordDao;
import com.kisro.cloud.pojo.VehicleRefuelingRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zoutao
 * @since 2023/3/1
 **/
@RestController
@RequestMapping("/new-driver")
public class NewDriverController {
    @Autowired
    private VehicleRefuelingRecordDao dao;

    @PostMapping("/add")
    public void insert(@RequestBody VehicleRefuelingRecord record) {
        dao.addVehicleRefuelingRecord(record);
    }

}

