package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * @author zoutao
 * @since 2023/4/24
 **/
@Data
public class ResultExport {
    @Excel(name = "有效应收报文数")
    private Long receivableCount = 0L;
    @Excel(name = "有效已收报文数")
    private Long receivedCount = 0L;
    @Excel(name = "累计已收报文数")
    private Long totalCount = 0L;
    @Excel(name = "VIN")
    private String vin;
    @Excel(name = "统计日期", exportFormat = "yyyy/MM/dd")
    private Date date;
}
