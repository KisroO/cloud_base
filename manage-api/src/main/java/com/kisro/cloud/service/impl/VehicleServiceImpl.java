package com.kisro.cloud.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kisro.cloud.mapper.VehicleMapper;
import com.kisro.cloud.pojo.Vehicle;
import com.kisro.cloud.service.IVehicleService;
import org.springframework.stereotype.Service;

/**
 * @author Kisro
 * @since 2022/12/23
 **/
@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements IVehicleService {
}
