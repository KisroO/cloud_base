package com.kisro.cloud.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kisro.cloud.common.EntityOperation;
import com.kisro.cloud.dao.VehicleMapper;
import com.kisro.cloud.pojo.Vehicle;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Kisro
 * @since 2022/11/7
 **/
@Service
public class VehicleService extends ServiceImpl<VehicleMapper, Vehicle> {
    @Resource
    private VehicleMapper vehicleMapper;

    public void batchInsert(List<Vehicle> list) {
        vehicleMapper.batchInsert(list);
    }

    public void insert(Vehicle vehicle) {
        EntityOperation.load(vehicleMapper)
                .handler(() -> vehicleMapper.customInsert(vehicle))
                .update();
    }
}
