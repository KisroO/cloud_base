package com.kisro.cloud.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kisro
 * @since 2023/1/4
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VehicleStatisticsVO {

    @ApiModelProperty("已接收上报车辆数")
    private Long receiveVehCount;

    @ApiModelProperty("TSP成功转发车辆总数")
    private Long dispatcherSuccessVehCount;

    @ApiModelProperty("TSP转发失败车辆总数")
    private Long dispatcherFailureVehCount;
}
