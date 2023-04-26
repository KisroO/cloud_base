package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * @author zoutao
 * @since 2023/4/19
 **/
@Data
public class LossRateResult {
    @Excel(name = "有效应收报文数")
    private Long receivableCount = 0L;
    @Excel(name = "有效已收报文数")
    private Long receivedCount = 0L;
    @Excel(name = "累计已收报文数")
    private Long totalCount = 0L;
    @Excel(name = "丢包率")
    private String lossRate = "0.0%";
//    private List<List<OriginalMessage>> lossPacketList = ListEx.newArrayList();
}
