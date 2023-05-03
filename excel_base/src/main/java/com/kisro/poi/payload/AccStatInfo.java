package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.kisro.poi.util.CommonUtils;
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
    @Excel(name = "开始时间", exportFormat = "yyyy/MM/dd HH:mm:ss",width = 20.0)
    private Date startDate;
    // 接收索引
    private int endIndex;
    // 结束时间(理论应收 不完整ACC ON-OFF)
    @Excel(name = "结束时间", exportFormat = "yyyy/MM/dd HH:mm:ss",width = 20.0)
    private Date endDate;
    // 已收报文数(完整ACC ON-OFF)
    @Excel(name = "累计已收报文数",type = 10)
    private int receivedCount = 0;
    // 应收报文数(理论应收)
    @Excel(name = "应收报文数",type = 10)
    private int receivableCount;
    // 区间累计实收实时与不发信息
    @Excel(name = "已收报文数",type = 10)
    private int totalCount;
    // 结束时间(理论实际应收 完整ACC ON-FF)
    @Excel(name = "离线时间(异常下线无离线时间)", exportFormat = "yyyy/MM/dd HH:mm:ss",width = 20.0)
    private Date logoutDate;
    // 区间内是否丢包
    private boolean hasLoss;
    // 区间内已收实时与补发数据 [开始时间,结束时间]
    private List<AccOriginMsg> partitionData = ListEx.newArrayList();

    /**
     * 计算应收与丢包信息
     */
    public void calculateInfo() {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        // 离线时间
        Date optionLogoutDate = Optional.ofNullable(logoutDate).orElse(startDate);
        long completeDateTime = optionLogoutDate.getTime();
        // 10s一帧实时信息
        // 计算已收与应收报文数
        long time = (endTime - startTime) / 1000;
        long actualTime = (completeDateTime - startTime) / 1000;
        // 最少应收
        int receivable = (int) Math.round((double) time / 10);
        // 理论已收
        int received = (int) Math.floor((double) actualTime / 10);
        setReceivedCount(received);
        if (receivable < receivedCount) {
            receivable = receivedCount;
        }
        // 计算完整ACC ON - OFF区间实收报文数
        long totalCount = partitionData.stream()
                .filter(item -> CommonUtils.checkDate(item.getAcquisitionTime(), startDate, optionLogoutDate))
                .count();
        setTotalCount((int) totalCount);
        setReceivableCount(receivable);
        setHasLoss(receivedCount < receivableCount);
    }
}
