package com.kisro.poi.service;

import com.kisro.poi.enums.Command;
import com.kisro.poi.payload.AccOriginMsg;
import com.kisro.poi.payload.AccStatInfo;
import com.kisro.poi.payload.LossRateResult;
import com.kisro.poi.payload.OriginalMessage;
import com.nex.bu1.lang.ObjEx;
import com.nex.bu1.lang.StrEx;
import com.nex.bu1.util.DateEx;
import com.nex.bu1.util.ListEx;
import com.nxe.galaxy.fv.protocol.util.MathUtils;
import io.netty.util.internal.MathUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author zoutao
 * @since 2023/4/25
 **/
@Service
@RequiredArgsConstructor
public class LossRateService {
    private static final String ON = "开";
    private static final String OFF = "关";
    private final HBaseService hBaseService;
    List<Integer> commandList = ListEx.newArrayList(0x01, 0x04,0x02, 0x03, 0xE8, 0xF4);
//    List<Integer> realtimeList = ListEx.newArrayList();

    public LossRateResult singleCarStat(String vin, Date date){
        //  初始化
        List<AccOriginMsg> dataList = ListEx.newArrayList();
        List<AccStatInfo> statInfoList = ListEx.newArrayList();
        // 1. 加载数据
        // todo 数据集扩大范围
        dataList = loadDataFromHBase(vin,commandList, date);
        // 统计结果
        LossRateResult result = new LossRateResult();
        int maxSize = dataList.size();
        AccStatInfo statInfo = null;
        for (int i = 0; i < maxSize; i++) {
            AccOriginMsg msg = dataList.get(i);
            String accFlag = msg.getAccFlag();
            Date acquisitionTime = msg.getAcquisitionTime();
            String commandStr = msg.getCommand();
            // 2. 查询下一个ACC=ON的位置报文，标记为起始点
            if (ObjEx.isNull(statInfo)) {
                // 位置报文，且为ON档
                if (Command.LOCATION_DATA_LIST.contains(commandStr) && StrEx.equals(ON, accFlag)) {
                    statInfo = new AccStatInfo();
                    statInfo.setStartDate(acquisitionTime);
                }
            } else {
                // 结束区间,位置报文且ACC状态 为 OFF
                if (Command.LOCATION_DATA_LIST.contains(commandStr) && StrEx.equals(OFF, accFlag)) {
                    statInfo.setEndDate(acquisitionTime);
                    statInfo.setLogoutDate(acquisitionTime);
                    statInfoList.add(statInfo);
                    statInfo = null;
                } else if (StrEx.equals(Command.VEHICLE_LOGOUT, commandStr)) {
                    // 登出报文, 结束区间统计
                    statInfo.setEndIndex(i);
                    statInfo.setEndDate(acquisitionTime);
                    statInfo.setLogoutDate(acquisitionTime);
                    statInfoList.add(statInfo);
                    statInfo = null;
                } else if(StrEx.equals(Command.VEHICLE_LOGIN, commandStr)){
                    // 异常下线，不设置下线时间
                    statInfo.setEndDate(acquisitionTime);
                    statInfoList.add(statInfo);
                    statInfo = null;
                } else if (Command.REAL_DATA_LIST.contains(commandStr)){
                    // 实时与补发信息
//                    statInfo.getPartitionData().add(msg);
                }
            }
        }
        // 5. 计算区间报文数
        processStatData(statInfoList,result);
        // 6. 计算丢包率
        calculateLossRate(result);
        return result;
    }

    private void processStatData(List<AccStatInfo> statInfoList,LossRateResult result) {
        AtomicLong totalReceivableCount = new AtomicLong();
        AtomicLong totalReceivedCount = new AtomicLong();
        AtomicLong totalCount = new AtomicLong();
        // todo 区间累计实收实时与补发总数
        statInfoList.stream()
                .peek(AccStatInfo::calculateInfo)
                .forEach(item -> {
                    totalReceivableCount.addAndGet(item.getReceivableCount());
                    totalReceivedCount.addAndGet(item.getReceivedCount());
                    totalCount.addAndGet(item.getTotalCount());
                });
        result.setReceivedCount(totalReceivedCount.get());
        result.setReceivableCount(totalReceivableCount.get());
        result.setTotalCount(totalCount.get());
    }

    private List<AccOriginMsg> loadDataFromHBase(String vin, List<Integer> commands, Date date) {
        Pair<Date, Date> datePair = betweenDates(date);
        List<OriginalMessage> list = hBaseService.findList(vin, datePair.getLeft(), datePair.getRight(), commands);
        List<AccOriginMsg> msgList = ListEx.newArrayList();
        list.stream()
                .filter(item -> ObjEx.isNotNull(item.getAcquisitionTime()))
                .forEach(item -> {
                    AccOriginMsg accOriginMsg = new AccOriginMsg();
                    BeanUtils.copyProperties(item, accOriginMsg);
                    msgList.add(accOriginMsg);
                });

        return msgList.stream()
                .filter(item -> checkDate(item.getAcquisitionTime(), datePair.getLeft(), datePair.getRight()))
                .sorted(Comparator.comparing(AccOriginMsg::getAcquisitionTime))
                .collect(Collectors.toList());
    }

    public Pair<Date, Date> betweenDates(Date statDate) {
        Date begin = DateEx.beginOfDay(statDate);
        Date end = DateEx.endOfDay(statDate);
        return Pair.of(begin, end);
    }

    public boolean checkDate(Date target, Date begin, Date end) {
        long targetTime = target.getTime();
        return targetTime > begin.getTime() && targetTime < end.getTime();
    }

    private void calculateLossRate(LossRateResult result){
        Long receivableCount = result.getReceivableCount();
        Long receivedCount = result.getReceivedCount();
        Long totalCount = result.getTotalCount();
        // 丢包率(规则一)
        BigDecimal receivableDecimal = new BigDecimal(receivableCount + "");
        BigDecimal receivedDecimal = new BigDecimal(receivedCount + "");
        BigDecimal percentageDecimal = new BigDecimal("100");
        BigDecimal rate1 = receivableDecimal.subtract(receivedDecimal)
                .divide(receivableDecimal, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(percentageDecimal);
        result.setLossRate1(rate1.toPlainString().concat("%"));
        // 丢包率(规则二)
        if(totalCount>receivableCount){
            return;
        }
        BigDecimal totalCountDecimal = new BigDecimal(totalCount + "");
        BigDecimal rate2 = receivableDecimal.subtract(totalCountDecimal)
                .divide(receivableDecimal, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(percentageDecimal);
        result.setLossRate2(rate2.toPlainString().concat("%"));
    }
}
