package com.kisro.poi.payload;

import com.kisro.poi.domain.OriginalMessage;
import com.nex.bu1.util.ListEx;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 单个完整登入登出区间信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatInfo {
    private Integer loginIndex;
    private Integer logoutIndex;
    private Date loginDate;
    private Date logoutDate;
    // 应收报文数
    private int receivableCount;
    // 已收报文数
    private int receivedCount;
    // 登入登出区间报文列表
    private List<OriginalMessage> partitionMessage = ListEx.newArrayList();
    // 该区间是否有丢包
    private boolean hasLossPacket;

    // todo 完善丢包统计
    public String packetLossRate() {
        return null;
    }

    public void calculateCount() {
        long loginTime = loginDate.getTime();
        long logoutTime = logoutDate.getTime();
        // 10s 一帧实时信息
        // 计算已收与应收报文数
        long time = (logoutTime - loginTime) / 1000;
        // 最少应收
        int receivable = (int) Math.floor((double) time / 10);
        int received = partitionMessage.size();
        if (receivable < received) {
            receivable = received;
        } else if (receivable > received) {
            // 可能存在丢包情况，重新计算应收

        }
        setReceivableCount(receivable);
        // 已收
        setReceivedCount(received);
        // 是否丢包
        setHasLossPacket(receivedCount < receivableCount);
    }
}
