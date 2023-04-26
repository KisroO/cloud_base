package com.kisro.cloud.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zoutao
 * @since 2023/3/3
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ImportVehicle {
    private String chassisNumber;

    private String deviceNumber;

    private String deviceModel;

    private String deviceCode;

    private String deviceVendor;

    private String simNumber;

    private String modelNumber;

    private String productionCode;

    private Long manufactureTime;

    private String iccid;
}
