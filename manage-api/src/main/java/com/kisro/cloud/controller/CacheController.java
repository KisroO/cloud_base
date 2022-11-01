package com.kisro.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.kisro.cloud.pojo.Report;
import com.kisro.cloud.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@RestController
@RequestMapping("/cache")
@Api(tags = "缓存接口")
@AllArgsConstructor
public class CacheController {
    private static final String CACHE_PREFIX = "report:";
    private final RedisUtil redisUtil;

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
}
