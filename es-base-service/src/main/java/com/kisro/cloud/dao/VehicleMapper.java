package com.kisro.cloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kisro.cloud.pojo.Vehicle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Kisro
 * @since 2022/11/7
 **/
public interface VehicleMapper extends BaseMapper<Vehicle> {
    void batchInsert(@Param("data") List<Vehicle> data);

    void customInsert(Vehicle vehicle);
}
