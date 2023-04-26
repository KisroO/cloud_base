package com.kisro.cloud.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.kisro.cloud.config.BusinessThreadPool;
import com.kisro.cloud.pojo.Report;
import com.kisro.cloud.pojo.VehicleStatisticsVO;
import com.kisro.cloud.pojo.bo.PlatformInfo;
import com.kisro.cloud.util.RedisUtil;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.lang.StrEx;
import com.nex.bu1.util.ListEx;
import com.nex.bu1.util.MapEx;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@RestController
@RequestMapping("/cache")
@Api(tags = "缓存接口")
//@AllArgsConstructor
public class CacheController {
    private static final String CACHE_PREFIX = "report:";
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String ONLINE_KEY = "online:test";

    @GetMapping("/info/{id}")
    @ApiOperation("查询缓存")
    public String cacheInfo(@PathVariable("id") Long id) {
        Report report = (Report) redisUtil.getCacheObject(CACHE_PREFIX + id);
        return JSONObject.toJSONString(report);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除缓存")
    public String deleteCacheInfo(@PathVariable("id") Long id) {
        boolean successFlag = redisUtil.deleteObject(CACHE_PREFIX + id);
        return successFlag ? "删除缓存成功" : "删除缓存失败";
    }

    @PostMapping("/add")
    @ApiOperation("添加缓存")
    public String addCache(@RequestBody Report report) {
        redisUtil.set(CACHE_PREFIX + report.getId(), report);
        return "添加缓存成功";
    }

    @PutMapping("/update")
    @ApiOperation("更新缓存")
    public String updateCache(@RequestBody Report report) {
        redisUtil.set(CACHE_PREFIX + report.getId(), report);
        return "update success";
    }

    @GetMapping("/stat")
    public String statData(@RequestParam("emissionLevel") String emissionLevel,
                           @RequestParam(value = "platform", required = false) String platform) {
        String emissionLevelStr = "国VI".equals(emissionLevel) ? "G6PT" : "G5PT";
        String platformKeyStr = Optional.ofNullable(platform).map(data -> platform).orElse("ALL");
        String redisKeyStr = "storage:statistic:".concat(emissionLevelStr);

        Object cacheObj = redisTemplate.opsForHash().get(redisKeyStr, platformKeyStr);
        VehicleStatisticsVO vo = Optional.ofNullable(cacheObj)
                .map(obj -> JSONObject.parseObject(obj.toString(), VehicleStatisticsVO.class))
                .orElse(new VehicleStatisticsVO(0L, 0L, 0L));
        BusinessThreadPool.THREAD_POOL_EXECUTOR.schedule(() -> {
            Long vehCount = new Random().nextLong();
            VehicleStatisticsVO statisticsVO = new VehicleStatisticsVO(vehCount, vehCount, 0L);
            redisTemplate.opsForHash().put(redisKeyStr, platformKeyStr, JSON.toJSONString(statisticsVO));
        }, 30L, TimeUnit.SECONDS);
        return JSONObject.toJSONString(vo);
    }

    @GetMapping("/onlineStatus")
    public String onlineStatus(@RequestParam(value = "vin", required = false) String vin,
                               @RequestParam(value = "controlFlag", required = false) boolean controlFlag) {
        boolean onlineFlag = false;
        if (controlFlag) {
            Object onlineCache = redisTemplate.opsForHash().get(ONLINE_KEY, vin);
//            List<JsonNode> jsonNodes = Optional.ofNullable(onlineCache)
            long lockSupportCount = Optional.ofNullable(onlineCache)
                    .map(Object::toString)
                    .map(obj -> JsonEx.parseArray(obj, JsonNode.class))
                    .orElse(ListEx.newArrayList())
                    .stream()
                    .filter(data -> JsonEx.getBooleanValue(data, "lockSupport"))
                    .count();
            onlineFlag = lockSupportCount > 0;
//            for (JsonNode jsonNode : jsonNodes) {
//                if (JsonEx.getBooleanValue(jsonNode, "lockSupport")) {
//                    onlineFlag = true;
//                }
//            }
        } else {
            onlineFlag = redisTemplate.opsForHash().hasKey(ONLINE_KEY, vin);
        }
        return onlineFlag ? "在线" : "离线";
    }

    @PostMapping("/online")
    public String online(@RequestParam("vin") String vin,
                         @RequestParam("iccid") String iccid,
                         @RequestParam("lockSupport") boolean lockSupport,
                         @RequestParam("online") boolean online) {
        String jsonStr = (String) redisTemplate.opsForHash().get(ONLINE_KEY, vin);
        List<JSONObject> registerInfoList;
        if (!StrEx.isBlank(jsonStr)) {
//            JsonNode value = JsonEx.parse(jsonStr);
//            registerInfoList = OBJECT_MAPPER.convertValue(value, LIST_MAP_REF);
            registerInfoList = JSON.parseArray(jsonStr, JSONObject.class);
        } else {
            registerInfoList = ListEx.newArrayList();
        }
        if (online) {
            JSONObject mapValue = registerInfoList.stream()
                    .filter(e -> StrEx.equals(MapEx.getString(e, "iccid"), iccid))
                    .findAny().orElseGet(() -> {
                        JSONObject emptyObj = new JSONObject();
                        registerInfoList.add(emptyObj);
                        return emptyObj;
                    });
            mapValue.put("iccid", iccid);
            mapValue.put("lockSupport", lockSupport);
            redisTemplate.opsForHash().put(ONLINE_KEY, vin, JsonEx.toJsonString(registerInfoList));
        } else {
            registerInfoList.removeIf(item -> iccid.equals(item.get("iccid")));
            if (registerInfoList.size() == 0) {
                redisTemplate.opsForHash().delete(ONLINE_KEY, vin);
            } else {
                redisTemplate.opsForHash().put(ONLINE_KEY, vin, JsonEx.toJsonString(registerInfoList));
            }
        }
        return "success";
    }

    @GetMapping("/platform")
    public String getPlatform() {
        String key = "platform:transmit:vehiclePlatform";
        String hashKey = "xys_zf_new";
        String obj = (String) redisTemplate.opsForHash().get(key, hashKey);
        String str = StrEx.toStr(obj);
        System.out.println(str);
        PlatformInfo info = JsonEx.parse(str, PlatformInfo.class);
        return JsonEx.toJsonString(info);
    }

    @GetMapping("/fuzzyQuery")
    public String fuzzyQuery(@RequestParam("chassisNumber") String chassisNumber) {
        Set<String> vinSet = ListEx.newHashSet();
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan("online:vehicle",
                ScanOptions.scanOptions().count(Integer.MAX_VALUE).match("*" + chassisNumber).build());
        while (cursor.hasNext()) {
            Map.Entry<Object, Object> next = cursor.next();
            String vin = next.getKey().toString();
            vinSet.add(vin);
        }
        return JsonEx.toJsonString(vinSet);
    }

}