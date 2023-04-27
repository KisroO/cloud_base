package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.nex.bu1.util.ListEx;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 统计区间
 *
 * @author zoutao
 * @since 2023/4/20
 **/
@Data
public class AccStatInfo {
    // 起始索引
    private int startIndex;
    // 起始时间
    @Excel(name = "开始时间", exportFormat = "yyyy/MM/dd HH:mm:ss")
    private Date startDate;
    // 接收索引
    private int endIndex;
    // 结束时间(理论应收 不完整ACC ON-OFF)
    @Excel(name = "结束时间", exportFormat = "yyyy/MM/dd HH:mm:ss")
    private Date endDate;
    // 已收报文数(完整ACC ON-OFF)
    @Excel(name = "实际已收报文数")
    private int receivedCount = 0;
    // 应收报文数(理论应收)
    @Excel(name = "理论应收报文数")
    private int receivableCount;
    // 区间累计实收实时与不发信息
    @Excel(name = "理论应收报文数")
    private int totalCount;
    // 结束时间(理论实际应收 完整ACC ON-FF)
    @Excel(name = "离线时间(异常下线无离线时间)", exportFormat = "yyyy/MM/dd HH:mm:ss")
    private Date logoutDate;
    // 区间内是否丢包
    private boolean hasLoss;

    private List<AccOriginMsg> partitionData = ListEx.newArrayList();

    /**
     * 计算应收与丢包信息
     */
    public void calculateInfo() {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        Long completeDateTime = Optional.ofNullable(logoutDate)
                .map(Date::getTime).orElse(startTime);
//        long completeDateTime = completeDate.getTime();
        // 10s 一帧实时信息
        // 计算已收与应收报文数
        long time = (endTime - startTime) / 1000;
        long actualTime = (completeDateTime - startTime) / 1000;
        // 最少应收
        int receivable = (int) Math.floor((double) time / 10);
        // 理论已收
        int received = (int) Math.floor((double) actualTime / 10);
        setReceivedCount(received);
        if (receivable < receivedCount) {
            receivable = receivedCount;
        }
        // todo 待优化，应计算完整ACC ON - OFF区间实收报文数
//        setTotalCount(partitionData.size());
        setReceivableCount(receivable);
        setHasLoss(receivedCount < receivableCount);
    }
}
