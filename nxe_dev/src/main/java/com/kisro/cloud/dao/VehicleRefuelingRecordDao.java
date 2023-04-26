package com.kisro.cloud.dao;

import com.kisro.cloud.pojo.VehicleRefuelingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zoutao
 * @since 2023/3/1
 **/
@Mapper
public interface VehicleRefuelingRecordDao {

    /**
     * 新增加油记录
     *
     * @param vehicleRefuelingRecord
     * @return
     */
    void addVehicleRefuelingRecord(VehicleRefuelingRecord vehicleRefuelingRecord);

    /**
     * 通过id更新转发状态
     *
     * @param id
     * @param sendStatus
     * @param failureReason
     */
    void updateVehicleRefuelingRecord(@Param("id") long id, @Param("sendStatus") String sendStatus, @Param("failureReason") String failureReason);

}