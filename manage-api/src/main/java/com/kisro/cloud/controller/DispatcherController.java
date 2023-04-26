package com.kisro.cloud.controller;

import com.kisro.cloud.pojo.bo.OnlineStatusVo;
import com.kisro.cloud.pojo.bo.PushStatus;
import com.kisro.cloud.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zoutao
 * @since 2023/2/24
 **/
@RestController
@RequestMapping("/dispatcher")
public class DispatcherController {

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/push/status")
    public String pushStatus(@RequestBody PushStatus pushStatus) {
        String iccid = pushStatus.getIccid();
        // 获取设备type
        OnlineStatusVo statusVo = OnlineStatusVo.builder()
                .iccid(iccid)
                .type(pushStatus.getType())
                .build();
        redisUtil.updateOnlineStatus("online", pushStatus.getVin(), statusVo, pushStatus.getStatus());
        return "SUCCESS";
    }

    @GetMapping("/status")
    public String online(PushStatus vo) {
        boolean online = false;
        if (StringUtils.isBlank(vo.getType())) {
            online = redisUtil.onlineStatus("online", vo.getVin(), null);
        } else {
            online = redisUtil.onlineStatus("online", vo.getVin(), data -> vo.getType().equals(data.getType()));
        }
        return online ? "ONLINE" : "OFFLINE";
    }

}
