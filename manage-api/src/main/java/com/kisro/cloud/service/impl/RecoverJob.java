package com.kisro.cloud.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSON;
import com.kisro.cloud.pojo.Sim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author zoutao
 * @since 2023/3/9
 **/
@Service
public class RecoverJob {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void exec(int sheet, int readRows) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Sim> dataList = null;
        try {
            ClassPathResource resource = new ClassPathResource("import/redis_" + sheet + ".xlsx");
            File file = resource.getFile();
            ImportParams params = new ImportParams();
            params.setHeadRows(1);
            params.setReadRows(readRows);
            dataList = ExcelImportUtil.importExcel(file, Sim.class, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        scanRedis(dataList);
    }

    private void scanRedis(List<Sim> dataList) {
        List<Object> objects = stringRedisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection rc) throws DataAccessException {
                rc.openPipeline();
                RedisHashCommands hashCommands = rc.hashCommands();
                for (Sim sim : dataList) {
                    String simNumber = Optional.ofNullable(sim.getSim()).orElse(null);
                    if (simNumber != null) {
                        hashCommands.hGet("basicinfo:bind:sim".getBytes(), simNumber.getBytes());
                    }
                }
                return null;
            }
        });
        System.out.println(JSON.toJSONString(objects));
    }
}
