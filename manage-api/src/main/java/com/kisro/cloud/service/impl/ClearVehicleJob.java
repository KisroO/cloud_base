package com.kisro.cloud.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSONObject;
import com.kisro.cloud.constant.RedisKey;
import com.kisro.cloud.pojo.InvalidVehicle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.List;

/**
 * @author zoutao
 * @since 2023/2/15
 **/
@Service
public class ClearVehicleJob {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 分批处理，
     *
     * @param sheet 默认1
     */
    public void clear(int sheet, int readRows) {
        // 1. 读取xlsx文件
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<InvalidVehicle> dataList = null;
        try {
            ClassPathResource resource = new ClassPathResource("files/redis_" + sheet + ".xlsx");
            File file = resource.getFile();
            ImportParams params = new ImportParams();
            params.setHeadRows(1);
            params.setReadRows(readRows);
            dataList = ExcelImportUtil.importExcel(file, InvalidVehicle.class, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 批量删除（pipeline+分批）
        execPipeline(dataList);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    private void execPipeline(List<InvalidVehicle> dataList) {
        List<Object> objects = stringRedisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection rc) throws DataAccessException {
                rc.openPipeline();
                RedisHashCommands hashCommands = rc.hashCommands();
                RedisSetCommands setCommands = rc.setCommands();
                for (InvalidVehicle vehicle : dataList) {
                    // 删除逻辑
//                    hashCommands.hDel("test".getBytes(), vehicle.getChassisNumber().getBytes());
//                    hashCommands.hDel("test:v1".getBytes(), vehicle.getVin().getBytes());
//                    setCommands.sRem("test:set".getBytes(), vehicle.getChassisNumber().getBytes());
                    String vin = vehicle.getVin();
                    String chassisNumber = vehicle.getChassisNumber();
                    String iccid = vehicle.getIccid();
                    String sim = vehicle.getSim();
                    String productionCode = vehicle.getProductionCode();
                    if (StringUtils.isNotBlank(vin)) {
                        byte[] vinBytes = vin.getBytes();
                        hashCommands.hDel(RedisKey.BASIC_INFO_BIND.getBytes(), vinBytes);
                        setCommands.sRem(RedisKey.BASIC_INFO_ITEM_VIN.getBytes(), vinBytes);
                        hashCommands.hDel(RedisKey.PLATFORM_TRANSMIT.getBytes(), vinBytes);
                        hashCommands.hDel(RedisKey.PLATFORM_TRANSMIT_HJ.getBytes(), vinBytes);
                    }
                    if (StringUtils.isNotBlank(chassisNumber)) {
                        byte[] chassisBytes = chassisNumber.getBytes();
                        hashCommands.hDel(RedisKey.BASIC_INFO_BIND_CHASSIS_NUMBER.getBytes(), chassisBytes);
                        hashCommands.hDel(RedisKey.BASIC_INFO_BIND_VIN_DEVICE_ID.getBytes(), chassisBytes);
                        setCommands.sRem(RedisKey.BASIC_INFO_ITEM_CHASSIS_NUMBER.getBytes(), chassisBytes);
                        setCommands.sRem(RedisKey.BASIC_INFO_ITEM_LOCK_DEVICE.getBytes(), chassisBytes);
                    }
                    if (StringUtils.isNotBlank(sim)) {
                        byte[] simBytes = sim.getBytes();
                        hashCommands.hDel(RedisKey.BASIC_INFO_BIND_SIM.getBytes(), simBytes);
                        hashCommands.hDel(RedisKey.BASIC_INFO_BIND_SIM_DEVICE_ID.getBytes(), simBytes);
                        hashCommands.hDel(RedisKey.BASIC_INFO_BIND_SIM_VIN.getBytes(), simBytes);
                    }
                    if (StringUtils.isNotBlank(iccid)) {
                        byte[] iccidBytes = iccid.getBytes();
                        setCommands.sRem(RedisKey.BASIC_INFO_ITEM_ICCID.getBytes(), iccidBytes);
                    }
                    if (StringUtils.isNotBlank(productionCode)) {
                        hashCommands.hDel(RedisKey.BASIC_INFO_BIND_PRODUCTION_CODE.getBytes(), productionCode.getBytes());
                    }
                }
                return null;
            }
        });
    }

    public void init() {
        long startVin = 10000001L;
        int startChassisNumber = 101;
        JSONObject v = new JSONObject();
        v.put("key", "test");
        String value = v.toJSONString();
        JSONObject v1 = new JSONObject();
        v1.put("key", "test,vin");
        String val = v1.toJSONString();
        for (int i = 0; i < 500; i++) {
            stringRedisTemplate.opsForHash().put("test", String.valueOf(startChassisNumber), value);
            stringRedisTemplate.opsForHash().put("test:v1", String.valueOf(startVin), val);
            stringRedisTemplate.opsForSet().add("test:set", String.valueOf(startChassisNumber), value);
            startVin++;
            startChassisNumber++;
        }
    }

    public void test() {
        stringRedisTemplate.opsForSet()
                .add("test:v1", "test");
    }
}
