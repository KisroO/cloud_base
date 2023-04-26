package com.kisro.cloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kisro.cloud.pojo.Vehicle;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Kisro
 * @since 2022/12/23
 **/
@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
}
