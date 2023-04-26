package com.kisro.cloud.util;

import com.alibaba.fastjson.JSONObject;
import com.kisro.cloud.pojo.bo.OnlineStatusVo;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.lang.ObjEx;
import com.nex.bu1.util.ListEx;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String KEY_PREFIX = "dispatcher:nd:";

    public void set(String k, Object v) {
        redisTemplate.opsForValue().set(k, JSONObject.toJSONString(v));
    }


    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Object getCacheObject(final String key) {
        ValueOperations operations = redisTemplate.opsForValue();
        return operations.get(key);
    }

    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    public void updateOnlineStatus(String key, String hashKey, OnlineStatusVo cacheVo, String status) {
        if ("ONLINE".equals(status)) {
            vehicleOnline(key, hashKey, cacheVo);
        } else {
            vehicleOffline(key, hashKey, cacheVo.getIccid());
        }
    }

    public void vehicleOnline(String key, String hashKey, OnlineStatusVo cacheVo) {
        HashOperations<String, Object, Object> hashOperator = redisTemplate.opsForHash();
        List<OnlineStatusVo> onlineDevices;
        if (hashOperator.hasKey(KEY_PREFIX + key, hashKey)) {
            String cacheStr = (String) hashOperator.get(KEY_PREFIX + key, hashKey);
            onlineDevices = JsonEx.parseArray(cacheStr, OnlineStatusVo.class);
            onlineDevices.add(cacheVo);
        } else {
            onlineDevices = ListEx.newArrayList(cacheVo);
        }
        hashOperator.put(KEY_PREFIX + key, hashKey, JsonEx.toJsonString(onlineDevices));
    }

    public void vehicleOffline(String key, String hashKey, String iccid) {
        HashOperations<String, Object, Object> hashOperator = redisTemplate.opsForHash();
        String cacheStr = (String) hashOperator.get(KEY_PREFIX + key, hashKey);
        List<OnlineStatusVo> onlineDevices = JsonEx.parseArray(cacheStr, OnlineStatusVo.class);
        onlineDevices.removeIf(data -> data.getIccid().equals(iccid));
        if (CollectionUtils.isEmpty(onlineDevices)) {
            hashOperator.delete(KEY_PREFIX + key, hashKey);
        } else {
            hashOperator.put(KEY_PREFIX + key, hashKey, JsonEx.toJsonString(onlineDevices));
        }
    }

    public boolean onlineStatus(String key, String hashKey, Predicate<OnlineStatusVo> predicate) {
        if (ObjEx.isNull(predicate)) {
            return redisTemplate.opsForHash().hasKey(KEY_PREFIX + key, hashKey);
        }
        Object cacheObj = redisTemplate.opsForHash().get(KEY_PREFIX + key, hashKey);
        return Optional.ofNullable(cacheObj)
                .map(obj -> (String) obj)
                .map(data -> JsonEx.parseArray(data, OnlineStatusVo.class))
                .orElseGet(ListEx::newArrayList)
                .stream()
                .anyMatch(predicate);
    }
}

