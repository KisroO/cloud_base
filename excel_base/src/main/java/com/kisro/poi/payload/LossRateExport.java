package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * @author zoutao
 * @since 2023/4/19
 **/
@Data
public class LossRateExport extends LossRateResult {
    @Excel(name = "VIN")
    private String vin;
    @Excel(name = "统计日期", exportFormat = "yyyy/MM/dd")
    private Date date;
}
