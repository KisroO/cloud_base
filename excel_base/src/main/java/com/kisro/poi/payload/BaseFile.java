package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * @author zoutao
 * @since 2023/4/20
 **/
@Data
public class BaseFile {
    @Excel(name = "车架号")
    private String vin;

    @Excel(name = "命令标识")
    private String command;

    @Excel(name = "终端采集时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date acquisitionTime;

    @Excel(name = "平台接收时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;
}
