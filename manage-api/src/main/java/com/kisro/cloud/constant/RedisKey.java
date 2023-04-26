package com.kisro.cloud.constant;

/**
 * @author zoutao
 * @since 2023/2/16
 **/
public interface RedisKey {
    String BASIC_INFO_BIND = "basicinfo:bind";
    String BASIC_INFO_BIND_CHASSIS_NUMBER = "basicinfo:bind:chassisNumber";
    String BASIC_INFO_BIND_PRODUCTION_CODE = "basicinfo:bind:productionCode";
    String BASIC_INFO_BIND_SIM = "basicinfo:bind:sim";
    String BASIC_INFO_BIND_SIM_DEVICE_ID = " basicinfo:bind:simDeviceId";
    String BASIC_INFO_BIND_SIM_VIN = "basicinfo:bind:simVin";
    String BASIC_INFO_BIND_VIN_DEVICE_ID = "basicinfo:bind:vinDeviceId";
    String BASIC_INFO_ITEM_CHASSIS_NUMBER = "basicinfo:item:chassisNumber";
    String BASIC_INFO_ITEM_ICCID = "basicinfo:item:iccid";
    String BASIC_INFO_ITEM_LOCK_DEVICE = "basicinfo:item:lockDevice";
    String BASIC_INFO_ITEM_VIN = "basicinfo:item:vin";
    String PLATFORM_TRANSMIT = "platform:transmit";
    String PLATFORM_TRANSMIT_HJ = "platform:transmit:hj";
}
