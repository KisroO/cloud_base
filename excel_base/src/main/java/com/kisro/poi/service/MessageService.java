package com.kisro.poi.service;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.kisro.poi.domain.OriginalMessage;
import com.kisro.poi.enums.Command;
import com.kisro.poi.payload.IndexPair;
import com.kisro.poi.payload.LossRateExport;
import com.kisro.poi.payload.LossRateResult;
import com.kisro.poi.payload.StatInfo;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.lang.StrEx;
import com.nex.bu1.util.DateEx;
import com.nex.bu1.util.ListEx;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zoutao
 * @since 2023/4/17
 **/
@Service
@RequiredArgsConstructor
public class MessageService {
    // 登入列表下标
    private Integer loginIndex = 0;
    // 登出列表下标
    private Integer logoutIndex = 0;
    // 登入列表，存储登入报文在其数据中的下标
    private List<Integer> loginIndices = ListEx.newArrayList();
    // 登出列表，存储登出报文在其数据中的下标
    private List<Integer> logoutIndices = ListEx.newArrayList();
    private Integer startIndex = 0;
    // 登入登出区间
    private List<IndexPair> betweenPartition = ListEx.newArrayList();
    // 登入登出区间下标
    private Integer partitionIndex = 0;
    // 当前登入登出区间
    private IndexPair currentPair = null;
    // 登入登出闭环区间统计列表
    private List<StatInfo> statInfoList = ListEx.newArrayList();
    // 丢包信息
    private List<List<OriginalMessage>> lossPacketInfoList = ListEx.newArrayList();

    // 总计应收报文数
    private long totalReceivableCount = 0L;
    // 总计已收报文数
    private long totalReceivedCount = 0L;
    // 丢包率
    private String lossRate = "";

    /**
     * 丢包率统计 第一版
     *
     * @param vin
     * @return
     */
    public String statPacketLossRate(String vin) {
        // 异常信息下标
        List<List<Integer>> exceptionIndexList = ListEx.newArrayList();
        // 异常信息
        List<List<OriginalMessage>> exceptionList = ListEx.newArrayList();
        // 可能丢包信息
        List<List<OriginalMessage>> packetLossList = ListEx.newArrayList();
        // 两帧实时信息间隔少于或大于10s
        List<List<OriginalMessage>> exceptionRealTimeList = ListEx.newArrayList();
        // xlsx报文数据
        List<OriginalMessage> dataList = loadData(vin);
//        init(dataList);
        int pre = 0;
        int current = 1;
        for (int i = 1; i < dataList.size(); i++) {
            if (current > dataList.size()) {
                break;
            }
            OriginalMessage preMessage = dataList.get(pre);
            OriginalMessage currentMessage = dataList.get(current);
            Long timeDiff = currentMessage.timeDiff(preMessage);
            String preCommand = preMessage.getCommand();
            String currentCommand = currentMessage.getCommand();
            Date preTime = preMessage.getAcquisitionTime();
            // 正常间隔10s
            long secondTimeDiff = timeDiff / 1000L;
            if (secondTimeDiff == 10) {
                pre = current;
                current++;
            } else {
                List<Integer> indexData = ListEx.newArrayList(pre, current);
                List<OriginalMessage> exceptData = ListEx.newArrayList(preMessage, currentMessage);
                // 异常情况1：命令相同，采集时间间隔不等于10s
                if (StrEx.equals(preCommand, currentCommand)) {
                    exceptData.add(new OriginalMessage("", preCommand, preTime));
                    indexData.add(current);
                    // 间隔大于10，丢包
                    if (secondTimeDiff > 10) {
                        packetLossList.add(ListEx.newArrayList(preMessage, currentMessage));
                    }
                    if (Command.REALTIME_DATA.equals(preCommand)) {
                        exceptionRealTimeList.add(ListEx.newArrayList(preMessage, currentMessage));
                    }
                }
                // 异常情况1：实时信息衔接登入报文
                if (Command.REALTIME_DATA.equals(preCommand) && Command.VEHICLE_LOGIN.equals(currentCommand)) {
                    packetLossList.add(ListEx.newArrayList(preMessage, currentMessage));
                    if (secondTimeDiff > 10) {
                        exceptionRealTimeList.add(ListEx.newArrayList(preMessage, currentMessage));
                    }
                }
                exceptionIndexList.add(indexData);
                exceptionList.add(exceptData);
                pre = current;
                current++;
            }
        }
//        System.out.println(JsonEx.toJsonString(exceptionList));
        List<List<OriginalMessage>> multiData = exceptionList.stream()
                .filter(item -> item.size() > 2)
                .collect(Collectors.toList());
        List<List<Integer>> multiIndexData = exceptionIndexList.stream()
                .filter(item -> item.size() > 2)
                .collect(Collectors.toList());

        System.out.println(JsonEx.toJsonString(multiData));
        System.out.println(JsonEx.toJsonString(multiIndexData));
//        System.out.println(JsonEx.toJsonString(dataList.get(34406)));
//        System.out.println(JsonEx.toJsonString(dataList.get(34407)));
        System.out.println(JsonEx.toJsonString(packetLossList));
        System.out.println("======异常实时信息数据");
        System.out.println(JsonEx.toJsonString(exceptionRealTimeList));
        return JsonEx.toJsonString(exceptionIndexList);
    }

    private List<OriginalMessage> loadData(String vin) {
        List<OriginalMessage> dataList = ListEx.newArrayList();
        try {
            ClassPathResource resource = new ClassPathResource("files/" + vin + ".xlsx");
//            File file = resource.getFile();
            ImportParams params = new ImportParams();
            params.setHeadRows(1);
            dataList = ExcelImportUtil.importExcel(resource.getInputStream(), OriginalMessage.class, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    private void initPartition(List<OriginalMessage> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            String commandStr = dataList.get(i).getCommand();
            if (StrEx.equals(Command.VEHICLE_LOGIN, commandStr)) {
                loginIndices.add(i);
            } else if (StrEx.equals(Command.VEHICLE_LOGOUT, commandStr)) {
                logoutIndices.add(i);
            }
        }
        int logoutSize = logoutIndices.size();
        int loginSize = loginIndices.size();
        int index = Math.max(loginSize, logoutSize);
        // 计算登出登出区间
        for (int i = 0; i < index; i++) {
            if (loginIndex >= loginSize || logoutIndex >= logoutSize) {
                break;
            }
            Integer left = loginIndices.get(loginIndex);
            Integer right = logoutIndices.get(logoutIndex);
            // 先登出后登入，登出下标+1 重新计算
            if (left > right) {
                logoutIndex += 1;
                continue;
            }
            if ((loginIndex + 1) < loginSize) {
                // 有异常下线，无登出报文，登出下标>登入下标+1的值
                Integer nextLeft = loginIndices.get(loginIndex + 1);
                if (right > nextLeft) {
                    loginIndex += 1;
                    continue;
                }
            }
            betweenPartition.add(IndexPair.of(left, right));
            loginIndex += 1;
            logoutIndex += 1;
        }
        // 重置
        loginIndex = 0;
        logoutIndex = 0;
    }

    /**
     * 正常报文丢包率统计（排除补发信息上报）
     * 完整登入登出闭环区间，计算其所有闭环区间内应收报文与已收报文
     * 丢包率 = 1 - (闭环区间已收报文数/闭环区间应收报文数)
     *
     * @param vin      VIN
     * @param statDate 统计日期
     * @return 闭环区间内数据
     */
    public LossRateResult normalStat(String vin, Date statDate) {
        LossRateResult result = new LossRateResult();
        // 加载数据
        List<OriginalMessage> dataList = loadData(vin);
        // 获取当天起始与结束时间
        Pair<Date, Date> pair = betweenDates(statDate);
        beforeReset();
        // 统计时间之内，排除补发信息
        List<OriginalMessage> targetDataList = dataList.stream()
                .filter(item -> checkDate(item.getAcquisitionTime(), pair.getLeft(), pair.getRight()))
                .filter(item -> StrEx.notEquals(Command.REALTIME_REUPLOAD_DATA, item.getCommand()))
                .collect(Collectors.toList());
        // 初始化登入登出区间
        initPartition(targetDataList);
        // 无统计数据，返回
        if (targetDataList.isEmpty()) {
            beforeReset();
            return result;
        }
        // 初始计算一次下标
        try {
            calculateNextIndex();
        } catch (Exception e) {
            beforeReset();
            afterReset();
            return result;
        }
        // 统计对象，存放单个完整登入登出区间记录
        StatInfo statInfo = null;
        int maxSize = targetDataList.size();
        for (int i = 0; i < maxSize; i++) {
            // 限界
            if (startIndex >= maxSize) {
                break;
            }
            // 1. 获取下一个的值
            OriginalMessage msg = targetDataList.get(startIndex);
            // 跳过补发信息(可补充) 以及非闭环数据
            if (msg.isReuploadData() || currentPair.checkBound(startIndex)) {
                startIndex += 1;
                continue;
            }
            // 登入命令, 构造一个新的统计对象
            if (msg.isLogin()) {
                statInfo = new StatInfo();
                statInfo.setLoginDate(msg.getAcquisitionTime());
                statInfo.setLoginIndex(startIndex);
                startIndex += 1;
            } else if (msg.isLogout()) {
                // 登出命令
                statInfo.setLogoutDate(msg.getAcquisitionTime());
                statInfo.setLogoutIndex(startIndex);
                // 加入统计列表
                statInfoList.add(statInfo);
                // 统计对象置空
                statInfo = null;
                // 区间下标+1, 计算下一个区间
                partitionIndex += 1;
                // 重新计算下标
                try {
                    calculateNextIndex();
                } catch (Exception e) {
                    break;
                }
            } else {
                // 实时数据,将其加入统计对象
                statInfo.getPartitionMessage().add(msg);
                startIndex += 1;
            }
        }
        // process 统计列表,统计应收与已收报文数，以及丢包率
        processNormalStat();
        // 提取丢包具体信息(可选)
        extraLossPacketInfo();
        result.setReceivableCount(totalReceivableCount);
        result.setReceivedCount(totalReceivedCount);
//        result.setLossRate(lossRate);
        if (CollectionUtils.isNotEmpty(lossPacketInfoList)) {
            System.out.println(vin);
            System.out.println(JsonEx.toJsonString(lossPacketInfoList));
        }
//        result.setLossPacketList(lossPacketInfoList);
        // 统计结束，重置对象
        afterReset();
        return result;
    }

    /**
     * 多日期多VIN统计
     */
    public List<LossRateExport> multiVehicleStat(List<String> vinList, Date startDate, Date endDate) {
        List<LossRateExport> dataList = ListEx.newArrayList();
        List<Date> dates = getDateList(startDate, endDate);
        for (Date date : dates) {
            for (String vin : vinList) {
                LossRateExport exportEntity = new LossRateExport();
                LossRateResult result = normalStat(vin, date);
                BeanUtils.copyProperties(result, exportEntity);
                exportEntity.setDate(date);
                exportEntity.setVin(vin);
                dataList.add(exportEntity);
            }
        }
        return dataList;
    }

    /**
     * 获取区间日期列表
     */
    private List<Date> getDateList(Date startDate, Date endDate) {
        LocalDate startBegin = DateEx.toLocalDate(DateEx.beginOfDay(startDate));
        LocalDate endBegin = DateEx.toLocalDate(DateEx.beginOfDay(endDate));
        long betweenDateNum = ChronoUnit.DAYS.between(startBegin, endBegin);
        return IntStream.iterate(0, i -> i + 1)
                .limit(betweenDateNum)
                .mapToObj(startBegin::plusDays)
                .map(DateEx::toDate)
                .collect(Collectors.toList());
    }

    /**
     * 提取丢包详情：丢包前后两帧数据
     */
    private void extraLossPacketInfo() {
        List<List<OriginalMessage>> lossPacketPartition = statInfoList.stream()
                .filter(StatInfo::isHasLossPacket)
                .map(StatInfo::getPartitionMessage)
                .collect(Collectors.toList());
        // 提取丢包的前后两帧实时信息
        System.out.println(JsonEx.toJsonString(lossPacketPartition));
        // todo 待验证
        for (List<OriginalMessage> list : lossPacketPartition) {
            for (int pre = 0, current = 1; current < list.size(); pre++, current++) {
                OriginalMessage preMsg = list.get(pre);
                OriginalMessage currentMsg = list.get(current);
                if ((currentMsg.timeDiff(preMsg) / 1000) > 10) {
                    lossPacketInfoList.add(ListEx.newArrayList(preMsg, currentMsg));
                }
            }
        }
        // todo 待验证前后两帧丢包个数与统计的丢包个数是否一致
        // 两帧前后时间差/10s 统计其与 （原应收-原已收） 是否一致
    }

    /**
     * 统计列表再处理：完整登入登出区间丢包计算
     */
    private void processNormalStat() {
        statInfoList.stream()
                .peek(StatInfo::calculateCount)
                .forEach(statInfo -> {
                    totalReceivableCount += statInfo.getReceivableCount();
                    totalReceivedCount += statInfo.getReceivedCount();
                });
        if (totalReceivedCount > totalReceivableCount) {
            totalReceivableCount = totalReceivedCount;
        }
        BigDecimal receivable = new BigDecimal(totalReceivableCount + "");
        BigDecimal received = new BigDecimal(totalReceivedCount + "");
        BigDecimal result = received.divide(receivable, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal d1 = new BigDecimal("1");
        lossRate = String.valueOf(d1.subtract(result).doubleValue()).concat("%");
    }

    private void afterReset() {
        logoutIndex = 0;
        loginIndex = 0;
        loginIndices = ListEx.newArrayList();
        logoutIndices = ListEx.newArrayList();
        startIndex = 0;
        betweenPartition = ListEx.newArrayList();
        currentPair = null;
        partitionIndex = 0;
//        statInfoList = ListEx.newArrayList();
    }

    private void beforeReset() {
        statInfoList = ListEx.newArrayList();
        totalReceivableCount = 0L;
        totalReceivedCount = 0L;
        lossRate = "";
        lossPacketInfoList = ListEx.newArrayList();
    }

    /**
     * 计算下一个下标值
     */
    private void calculateNextIndex() {
        if (partitionIndex >= betweenPartition.size()) {
            throw new IllegalArgumentException("partitionIndex out of bound,index:" + partitionIndex);
        }
        currentPair = betweenPartition.get(partitionIndex);
        loginIndex = currentPair.getLeft();
        logoutIndex = currentPair.getRight();
        startIndex = loginIndex;
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
}
