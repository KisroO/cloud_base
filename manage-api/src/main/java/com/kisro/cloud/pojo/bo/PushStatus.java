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
public class PushStatus {
    private String vin;

    private String iccid;

    private String type;

    private String status;
}
