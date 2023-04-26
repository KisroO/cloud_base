package com.kisro.cloud.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * @author zoutao
 * @since 2023/2/16
 **/
@Data
public class InvalidVehicle {
    @Excel(name = "vin")
    private String vin;
    @Excel(name = "iccid")
    private String iccid;
    @Excel(name = "sim")
    private String sim;
    @Excel(name = "chassis_number")
    private String chassisNumber;
    @Excel(name = "production_code")
    private String productionCode;

}
