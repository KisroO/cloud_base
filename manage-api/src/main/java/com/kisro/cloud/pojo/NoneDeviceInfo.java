package com.kisro.cloud.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * @author zoutao
 * @since 2023/3/22
 **/
@Data
public class NoneDeviceInfo {
    @Excel(name = "vin")
    private String vin;

    @Excel(name = "chassis_number")
    private String chassisNumber;

    @Excel(name = "company")
    private String company;

}
