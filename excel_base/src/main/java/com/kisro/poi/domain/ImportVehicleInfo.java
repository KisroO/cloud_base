package com.kisro.poi.domain;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zoutao
 * @since 2023/4/6
 **/
@Data
public class ImportVehicleInfo {
    @Excel(name = "VIN")
    @NotBlank(message = "底盘号不能为空")
    private String vin;

    @Excel(name = "底盘号")
    @NotBlank(message = "底盘号不能为空")
    private String chassisNumber;

    @Excel(name = "SN")
    @NotNull(message = "设备编号不能为空")
    private String sn;

    @Excel(name = "托管企业")
    private String managedEnterprise;
}
