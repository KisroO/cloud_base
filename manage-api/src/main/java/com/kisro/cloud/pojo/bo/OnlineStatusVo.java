package com.kisro.cloud.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zoutao
 * @since 2023/2/24
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnlineStatusVo {
    private String iccid;

    private String type;
}
