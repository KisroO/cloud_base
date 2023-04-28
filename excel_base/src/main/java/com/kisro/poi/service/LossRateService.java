package com.kisro.poi.service;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.kisro.poi.enums.Command;
import com.kisro.poi.payload.*;
import com.kisro.poi.util.CommonUtils;
import com.kisro.poi.util.ExportUtils;
import com.nex.bu1.io.export.ExportEx;
import com.nex.bu1.lang.ObjEx;
import com.nex.bu1.lang.StrEx;
import com.nex.bu1.util.DateEx;
import com.nex.bu1.util.ListEx;
import com.nex.bu1.util.MapEx;
import com.nxe.galaxy.dynamic.commons.entity.OriginalMessageRecord;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private static final List<Integer> COMMAND_LIST = ListEx.newArrayList(0x01, 0x02, 0x03, 0x04, 0xE8, 0xF4);
    private static final List<Integer> REUPLOAD_DATA_LIST = ListEx.newArrayList(0x03, 0xF4);

    /**
     * 单车单日统计
     *
     * @param vin
     * @param date
     * @return
     */
    public LossRateExport singleCarStat(String vin, Date date) {
        //  初始化
        List<AccOriginMsg> dataList;
        // 统计结果
        LossRateExport result = new LossRateExport();
        result.setVin(vin);
        result.setDate(date);
        List<AccStatInfo> statInfoList = ListEx.newArrayList();
        // 1. 加载数据
//        dataList = loadDataFromHBase(vin,commandList, date);
        dataList = loadData(vin, COMMAND_LIST, date);
        generalStat(dataList, statInfoList);
        // 5. 计算区间报文数
        processStatData(statInfoList, result);
        // 6. 计算丢包率
        calculateLossRate(result);
        dataList = null;
        return result;
    }

    /**
     * 单车单日统计(带统计详情数据)
     *
     * @param vin            VIN
     * @param date           统计日期
     * @param needDetailFlag 是否导出统计详情
     * @return 统计扩展数据
     */
    public LossRateResultExp singleStat(String vin, Date date, Boolean needDetailFlag) {
        List<AccStatInfo> statInfoList = ListEx.newArrayList();
        LossRateExport result = new LossRateExport();
        result.setVin(vin);
        result.setDate(date);
        // 1. 加载数据
        List<AccOriginMsg> dataList = loadData(vin, COMMAND_LIST, date);
        LossRateResultExp resultExp = new LossRateResultExp();
        // 统计
        generalStat(dataList, statInfoList);
        // 计算区间报文数
        processStatData(statInfoList, result);
        // 计算丢包率
        calculateLossRate(result);
        dataList = null;
        resultExp.setResult(result);
        // 导出统计详情
        if (needDetailFlag) {
            resultExp.setDetailList(statInfoList);
        }
        return resultExp;
    }

    public void generalStat(List<AccOriginMsg> dataList, List<AccStatInfo> statInfoList) {
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
                } else if (StrEx.equals(Command.VEHICLE_LOGIN, commandStr)) {
                    // 异常下线，不设置下线时间
                    statInfo.setEndDate(acquisitionTime);
                    statInfoList.add(statInfo);
                    statInfo = null;
                } else if (Command.REAL_DATA_LIST.contains(commandStr)) {
                    // 实时与补发信息
                    statInfo.getPartitionData().add(msg);
                }
            }
        }
    }

    private void processStatData(List<AccStatInfo> statInfoList, LossRateResult result) {
        AtomicLong totalReceivableCount = new AtomicLong();
        AtomicLong totalReceivedCount = new AtomicLong();
        AtomicLong totalCount = new AtomicLong();
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

    /**
     * 第一版
     *
     * @param vin
     * @param commands
     * @param date
     * @return
     */
    private List<AccOriginMsg> loadDataFromHBase(String vin, List<Integer> commands, Date date) {
        Pair<Date, Date> datePair = betweenDates(date);
        List<AccOriginMsg> msgList = hBaseService.findList(vin, datePair.getLeft(), datePair.getRight(), commands);
//        List<AccOriginMsg> msgList = ListEx.newArrayList();
//        list.stream()
//                .filter(item -> ObjEx.isNotNull(item.getAcquisitionTime()))
//                .forEach(item -> {
//                    AccOriginMsg accOriginMsg = new AccOriginMsg();
//                    BeanUtils.copyProperties(item, accOriginMsg);
//                    msgList.add(accOriginMsg);
//                });
        // todo 数据过滤优化
        return msgList.stream()
                .filter(item -> CommonUtils.checkDate(item.getAcquisitionTime(), datePair.getLeft(), datePair.getRight()))
                .sorted(Comparator.comparing(AccOriginMsg::getAcquisitionTime))
                .collect(Collectors.toList());
    }

    /**
     * 加载原始报文并转换为统计对象
     * 第二版
     *
     * @param vin
     * @param commands 命令
     * @param date     报文日期(查询时根据接受时间查询)
     * @return
     */
    private List<AccOriginMsg> loadData(String vin, List<Integer> commands, Date date) {
        Pair<Date, Date> datePair = betweenDates(date);
        // 获取统计日期当天的数据
        List<OriginalMessageRecord> list = hBaseService.findOriginalList(vin, datePair.getLeft(), datePair.getRight(), commands);
        if (CollectionUtils.isNotEmpty(list)) {
            // 继续获取补发与位置盲区数据(最多获取未来7天)
            List<Date> dateList = CommonUtils.getRecentSevenDate(date);
            for (Date nextDate : dateList) {
                Pair<Date, Date> pair = CommonUtils.betweenDates(nextDate);
                // 获取下一天的补发与位置盲区数据
                Pair<List<OriginalMessageRecord>, Boolean> nextDataPair = hBaseService.findNextReUploadData(vin, pair.getLeft(), pair.getRight(), REUPLOAD_DATA_LIST, date);
                list.addAll(nextDataPair.getLeft());
                if (!nextDataPair.getRight()) {
                    break;
                }
            }
        }
        List<AccOriginMsg> resList = hBaseService.processOriginalMessage(list, vin);
        list = null;
        return resList.stream()
                .sorted(Comparator.comparing(AccOriginMsg::getAcquisitionTime))
                .collect(Collectors.toList());
    }

    /**
     * 数据处理
     *
     * @param statDate
     * @return
     */
    public Pair<Date, Date> betweenDates(Date statDate) {
        Date begin = DateEx.beginOfDay(statDate);
        Date end = DateEx.endOfDay(statDate);
        return Pair.of(begin, end);
    }

    /**
     * 计算丢包率
     *
     * @param result
     */
    private void calculateLossRate(LossRateResult result) {
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
        if (totalCount > receivableCount) {
            return;
        }
        BigDecimal totalCountDecimal = new BigDecimal(totalCount + "");
        BigDecimal rate2 = receivableDecimal.subtract(totalCountDecimal)
                .divide(receivableDecimal, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(percentageDecimal);
        result.setLossRate2(rate2.toPlainString().concat("%"));
    }

    public void exportStatInfo(List<LossRateExport> resultList, HttpServletResponse response) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        String vin = resultList.get(0).getVin();
        ExportParams exportParams = new ExportParams();
        exportParams.setSheetName(exportStatFileName(vin));
        try {
            ExportEx.exportExcel(response, exportStatFileName(vin), resultList, LossRateExport.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportSingleStat(String vin, Date date, boolean needDetailFlag, HttpServletResponse response) {
        LossRateResultExp lossRateResultExp = singleStat(vin, date, needDetailFlag);
        LossRateExport result = lossRateResultExp.getResult();
        List<AccStatInfo> detailList = null;
        if (needDetailFlag) {
            detailList = lossRateResultExp.getDetailList();
        }
        if (ObjEx.isNull(result)) {
            return;
        }
        List<LossRateExport> resultList = ListEx.newArrayList(result);
        String fileName = exportStatFileName(vin).concat(".xlsx");
        List<Map<String, Object>> sheetList = ListEx.newArrayList();

        ExportParams lossExportParam = new ExportParams();
        lossExportParam.setSheetName("丢包率");

        Map<String, Object> lossSheetMap = MapEx.newHashMap();
        lossSheetMap.put("title", lossExportParam);
        lossSheetMap.put("entity", LossRateExport.class);
        lossSheetMap.put("data", resultList);
        sheetList.add(lossSheetMap);

        if(CollectionUtils.isNotEmpty(detailList)){
            ExportParams detailExportParam = new ExportParams();
            detailExportParam.setSheetName("统计区间信息");

            Map<String, Object> detailSheetMap = MapEx.newHashMap();
            detailSheetMap.put("title", detailExportParam);
            detailSheetMap.put("entity", AccStatInfo.class);
            detailSheetMap.put("data", detailList);
            sheetList.add(detailSheetMap);
        }
        ExportUtils.exportMultiSheet(sheetList, fileName, response);
    }

    private String exportStatFileName(String vin) {
        return vin.concat("_丢包率");
    }
}
