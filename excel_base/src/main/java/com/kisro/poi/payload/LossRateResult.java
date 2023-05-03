package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * @author zoutao
 * @since 2023/4/19
 **/
@Data
public class LossRateResult {
    @Excel(name = "理论应收报文数",type = 10)
    private long receivableCount = 0L;
    @Excel(name = "实际已收报文数",type = 10)
    private long receivedCount = 0L;
    @Excel(name = "累计已收报文数",type = 10)
    private long totalCount = 0L;
    @Excel(name = "丢包率(规则一)")
    private String lossRate1 = "0.0%";
    @Excel(name = "丢包率(规则二)")
    private String lossRate2 = "0.0%";
//    private List<List<OriginalMessage>> lossPacketList = ListEx.newArrayList();
}
