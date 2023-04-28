package com.kisro.poi.payload;

import lombok.Data;

import java.util.List;

/**
 * @author zoutao
 * @date 2023/4/28
 */
@Data
public class LossRateResultExp {
    /**
     * 丢包信息
     */
    private LossRateExport result;
    /**
     * 统计详情(统计区间)
     */
    private List<AccStatInfo> detailList;
}
