package com.kisro.poi.domain;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.kisro.poi.enums.FuelTgTypeEnum;
import com.nex.bu1.io.export.Csv;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author zoutao
 * @since 2023/4/11
 **/
@Setter
@Getter
public class VehicleInOem {

    @Excel(name = "VIN")
    @Csv(name = "VIN")
    String vin;
    @Excel(name = "底盘号")
    @Csv(name = "底盘号")
    String chassisNumber;
    @Csv(name = "SN")
    @Excel(name = "SN")
    String deviceSn;
    @Csv(name = "终端型号")
    @Excel(name = "终端型号")
    String deviceModel;
    @Csv(name = "设备供应商")
    @Excel(name = "设备供应商")
    String deviceManufacture;
    @Csv(name = "SIM卡条形码")
    @Excel(name = "SIM卡条形码")
    String simBarCode;
    @Csv(name = "SIM")
    @Excel(name = "SIM")
    String simPhoneNumber;
    @Csv(name = "发动机号")
    @Excel(name = "发动机号")
    String engineNumber;
    @Csv(name = "公告型号")
    @Excel(name = "公告型号")
    String model;
    @Csv(name = "车型代码")
    @Excel(name = "车型代码")
    String vehicleModel;
    @Csv(name = "发动机型号")
    @Excel(name = "发动机型号")
    String engineModel;
    @Csv(name = "车牌号")
    @Excel(name = "车牌号")
    String licensePlate;
    @Csv(name = "下线时间", format = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "下线时间", format = "yyyy-MM-dd HH:mm:ss")
    Date offLineDate;
    @Csv(name = "排放水平")
    @Excel(name = "排放水平")
    String emissionLevel;


    private String iccid;
    Integer bindDeviceNumber;
    String powerType;
    String productionCode;
    @Csv(name = "动力类型")
    @Excel(name = "动力类型")
    String powerTypeName;
    @Csv(name = "托管企业")
    @Excel(name = "托管企业")
    String managedEnterprise;
    @Csv(name = "设备类型")
    @Excel(name = "设备类型")
    String deviceType;

    public void setPowerType(String powerType) {
        this.powerType = powerType;
        powerTypeName = FuelTgTypeEnum.getNameByCode(powerType);
    }
}
