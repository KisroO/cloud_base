package com.kisro.cloud.pojo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zoutao
 * @since 2023/3/1
 **/
@Data
@Builder
public class VehicleRefuelingRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * VIN
     */
    private String vin;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 加油标志(0：没加油； 1：加油)
     */
    private byte refuelingSign;
    /**
     * 主油箱液位差
     */
    private byte mainTankLevelDifference;
    /**
     * 主副油箱标志（0：主油箱；1：副油箱）
     */
    private Byte tankSign;
    /**
     * 转发状态(SUCCESS：成功，FAILURE：失败)
     */
    private String sendStatus;
    /**
     * 失败原因
     */
    private String failureReason;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

}
