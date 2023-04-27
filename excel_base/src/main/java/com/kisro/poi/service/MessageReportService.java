package com.kisro.poi.service;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.kisro.poi.enums.Command;
import com.kisro.poi.payload.*;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.lang.ObjEx;
import com.nex.bu1.lang.StrEx;
import com.nex.bu1.util.DateEx;
import com.nex.bu1.util.ListEx;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zoutao
 * @since 2023/4/20
 **/
@Service
@RequiredArgsConstructor
public class MessageReportService {
    private static final String ON = "开";
    private static final String OFF = "关";
    private final HBaseService hBaseService;
    List<AccOriginMsg> dataList = ListEx.newArrayList();
    List<AccStatInfo> statInfoList = ListEx.newArrayList();
    //    List<Integer> commandList = ListEx.newArrayList(0x01,0x02,0x03,0x04,0xE8,0xF4);
    // 实时数据目前只获取总条数
    List<Integer> commandList = ListEx.newArrayList(0x01, 0x04, 0xE8, 0xF4);
    List<Integer> realtimeList = ListEx.newArrayList(0x02, 0x03);
    // 下一个访问下标
    int nextIndex = 0;
    // 区间起始下标
    int startIndex = 0;
    // 累计区间应收
    long totalReceivedCount = 0;
    // 累计区间已收
    long totalReceivableCount = 0;
    // 累计已收(实时+补发)
    long totalCount = 0;


    public LossRateResult newStat(String vin, Date date) {
        beforeReset();
        // 1. 加载数据
        // 后续可从HBase中获取
        loadData(vin, date);
        LossRateResult result = new LossRateResult();
        int maxSize = dataList.size();
        AccStatInfo statInfo = null;
//        AccStatInfo preStatInfo = new AccStatInfo();;
        for (nextIndex = 0; nextIndex < maxSize; nextIndex++) {
            AccOriginMsg msg = dataList.get(nextIndex);
            String accFlag = msg.getAccFlag();
            Date acquisitionTime = msg.getAcquisitionTime();
            String commandStr = msg.getCommand();
            // 统计累计已收实时与补发
            if (StrEx.equals(Command.REALTIME_DATA, commandStr) || StrEx.equals(Command.REALTIME_REUPLOAD_DATA, commandStr)) {
                totalCount += 1;
            }
            // 2. 查询下一个ACC=ON的位置报文，标记为起始点
            if (ObjEx.isNull(statInfo)) {
                // 位置报文，且为ON档
                if (Command.LOCATION_DATA_LIST.contains(commandStr) && StrEx.equals(ON, accFlag)) {
                    statInfo = new AccStatInfo();
                    statInfo.setStartIndex(nextIndex);
                    statInfo.setStartDate(acquisitionTime);
                }
            } else {
                // 结束区间
                if (Command.LOCATION_DATA_LIST.contains(commandStr) && StrEx.equals(OFF, accFlag)) {
                    statInfo.setEndIndex(nextIndex);
                    statInfo.setEndDate(acquisitionTime);
                    statInfo.setLogoutDate(acquisitionTime);
                    statInfoList.add(statInfo);
                    // 保存区间信息
//                    BeanUtils.copyProperties(statInfo, preStatInfo);
                    statInfo = null;
//                } else if (StrEx.equals(Command.VEHICLE_LOGOUT, commandStr) || StrEx.equals(Command.VEHICLE_LOGIN, commandStr)) {
                } else if (StrEx.equals(Command.VEHICLE_LOGOUT, commandStr)) {
                    // 碰到登出, 结束区间统计
                    statInfo.setEndIndex(nextIndex);
                    statInfo.setEndDate(acquisitionTime);
                    statInfo.setLogoutDate(acquisitionTime);
                    statInfoList.add(statInfo);
                    //
//                    BeanUtils.copyProperties(statInfo, preStatInfo);
                    statInfo = null;
                } else if (Command.REAL_DATA_LIST.contains(commandStr)) {
                    // 实时信息与补发信息
//                    statInfo.setReceivedCount(statInfo.getReceivedCount() + 1);
                } else if (StrEx.equals(Command.VEHICLE_LOGIN, commandStr)) {
                    // 上个区间正常登出，衔接补发|位置盲区，再衔接登入
//                    if(preStatInfo.getLogoutDate() != null){
//                        continue;
//                    }
                    // 异常下线
                    statInfo.setEndIndex(nextIndex);
                    statInfo.setEndDate(acquisitionTime);

                    statInfoList.add(statInfo);
//                    preStatInfo = new AccStatInfo();
                    statInfo = null;
                }
            }
        }
        // 5. 计算区间应收与已收报文数
        postProcessData(vin);
        result.setReceivableCount(totalReceivableCount);
        result.setReceivedCount(totalReceivedCount);
        result.setTotalCount(totalCount);
        afterReset();
        return result;
    }

    public LossRateResult newStatV2(String vin, Date date) {
        beforeReset();
        // 1. 加载数据
        // 后续可从HBase中获取
        dataList = loadDataFromHBase(vin, commandList, date);

        System.out.println(dataList.size());
        LossRateResult result = new LossRateResult();
        int maxSize = dataList.size();
        AccStatInfo statInfo = null;
//        AccStatInfo preStatInfo = new AccStatInfo();;
        for (nextIndex = 0; nextIndex < maxSize; nextIndex++) {
            AccOriginMsg msg = dataList.get(nextIndex);
            String accFlag = msg.getAccFlag();
            Date acquisitionTime = msg.getAcquisitionTime();
            String commandStr = msg.getCommand();
            // 统计累计已收实时与补发
            if (StrEx.equals(Command.REALTIME_DATA, commandStr) || StrEx.equals(Command.REALTIME_REUPLOAD_DATA, commandStr)) {
                totalCount += 1;
            }
            // 2. 查询下一个ACC=ON的位置报文，标记为起始点
            if (ObjEx.isNull(statInfo)) {
                // 位置报文，且为ON档
                if (Command.LOCATION_DATA_LIST.contains(commandStr) && StrEx.equals(ON, accFlag)) {
                    statInfo = new AccStatInfo();
                    statInfo.setStartIndex(nextIndex);
                    statInfo.setStartDate(acquisitionTime);
                }
            } else {
                // 结束区间
                if (Command.LOCATION_DATA_LIST.contains(commandStr) && StrEx.equals(OFF, accFlag)) {
                    statInfo.setEndIndex(nextIndex);
                    statInfo.setEndDate(acquisitionTime);
                    statInfo.setLogoutDate(acquisitionTime);
                    statInfoList.add(statInfo);
                    // 保存区间信息
//                    BeanUtils.copyProperties(statInfo, preStatInfo);
                    statInfo = null;
//                } else if (StrEx.equals(Command.VEHICLE_LOGOUT, commandStr) || StrEx.equals(Command.VEHICLE_LOGIN, commandStr)) {
                } else if (StrEx.equals(Command.VEHICLE_LOGOUT, commandStr)) {
                    // 碰到登出, 结束区间统计
                    statInfo.setEndIndex(nextIndex);
                    statInfo.setEndDate(acquisitionTime);
                    statInfo.setLogoutDate(acquisitionTime);
                    statInfoList.add(statInfo);
                    //
//                    BeanUtils.copyProperties(statInfo, preStatInfo);
                    statInfo = null;
                } else if (Command.REAL_DATA_LIST.contains(commandStr)) {
                    // 实时信息与补发信息
//                    statInfo.setReceivedCount(statInfo.getReceivedCount() + 1);
                } else if (StrEx.equals(Command.VEHICLE_LOGIN, commandStr)) {
                    // 上个区间正常登出，衔接补发|位置盲区，再衔接登入
//                    if(preStatInfo.getLogoutDate() != null){
//                        continue;
//                    }
                    // 异常下线
                    statInfo.setEndIndex(nextIndex);
                    statInfo.setEndDate(acquisitionTime);

                    statInfoList.add(statInfo);
//                    preStatInfo = new AccStatInfo();
                    statInfo = null;
                }
            }
        }
        // 5. 计算区间应收与已收报文数
        postProcessData(vin);
        result.setReceivableCount(totalReceivableCount);
        result.setReceivedCount(totalReceivedCount);
        int realtimeDataSize = loadDataFromHBase(vin, realtimeList, date).size();
        result.setTotalCount((long) realtimeDataSize);
//        if (CollectionUtils.isNotEmpty(dataList)) {
//            try {
//                exportOriginMsg(vin, date, dataList);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        calculateLossRate(result);
        afterReset();
        return result;
    }

    public List<LossRateExport> newMultiStatV2(List<String> vinList,Date startDate,Date endDate){
        List<LossRateExport> resultList = ListEx.newArrayList();
        List<Date> dates = getDateList(startDate, endDate);
        for (String vin : vinList) {
            for (Date date : dates) {
                LossRateExport export = new LossRateExport();
                LossRateResult result = newStatV2(vin, date);
                BeanUtils.copyProperties(result, export);
                export.setVin(vin);
                export.setDate(date);
                resultList.add(export);
            }
        }
        return resultList;
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
    private void exportStatInfo(Workbook workbook, String vin, Date date) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(vin + "_" + DateEx.format(date, DateEx.FMT_YMD2) + "_统计.xlsx");
            workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private void exportOriginMsg(String vin, Date date, List<AccOriginMsg> dataList) throws IOException {
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), AccOriginMsg.class, dataList);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(vin + "_" + DateEx.format(date, DateEx.FMT_YMD2) + ".xlsx");
            workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private List<AccOriginMsg> loadDataFromHBase(String vin, List<Integer> commands, Date date) {
        String dateStr = DateEx.format(date, DateEx.FMT_YMD2);
//        List<AccOriginMsg> originMsgList;
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

    private Pair<Date, Date> betweenDateV2(Date date) {
        Date begin = DateEx.beginOfDay(date);
        Date end = DateEx.plusDay(begin, 1);
        return Pair.of(begin, end);
    }

    private void beforeReset() {
        dataList = ListEx.newArrayList();
        statInfoList = ListEx.newArrayList();
    }

    private void afterReset() {
        nextIndex = 0;
        startIndex = 0;
        totalReceivableCount = 0;
        totalReceivedCount = 0;
        totalCount = 0;
    }

    private void postProcessData(String vin) {
        statInfoList.stream()
                .peek(AccStatInfo::calculateInfo)
                .forEach(item -> {
                    totalReceivableCount += item.getReceivableCount();
                    totalReceivedCount += item.getReceivedCount();
                });
        System.out.println(JsonEx.toJsonString(statInfoList));
        List<AccStatInfo> exportList = ListEx.newArrayList();
        exportList.addAll(statInfoList);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), AccStatInfo.class, statInfoList);
        // todo 待完善
        try {
            exportStatInfo(workbook, vin, DateEx.now());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<AccStatInfo> abnormalList = statInfoList.stream()
                .filter(AccStatInfo::isHasLoss)
                .collect(Collectors.toList());
        if (totalReceivableCount < totalReceivedCount) {
            totalReceivableCount = totalReceivedCount;
        }
    }

    public Pair<Date, Date> betweenDates(Date statDate) {
        Date begin = DateEx.beginOfDay(statDate);
        Date end = DateEx.endOfDay(statDate);
        return Pair.of(begin, end);
    }

    private void loadData(String vin, Date date) {
        String dateStr = DateEx.format(date, DateEx.FMT_YMD2);
        List<AccOriginMsg> originMsgList;
        Pair<Date, Date> datePair = betweenDates(date);
        try {
            ClassPathResource resource = new ClassPathResource("20230420/" + vin + "_" + dateStr + ".xlsx");
            ImportParams params = new ImportParams();
            params.setHeadRows(1);
            originMsgList = ExcelImportUtil.importExcel(resource.getInputStream(), AccOriginMsg.class, params);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        dataList = originMsgList.stream()
                .filter(item -> checkDate(item.getAcquisitionTime(), datePair.getLeft(), datePair.getRight()))
                .collect(Collectors.toList());
    }

    public boolean checkDate(Date target, Date begin, Date end) {
        long targetTime = target.getTime();
        return targetTime > begin.getTime() && targetTime < end.getTime();
    }

    public List<LossRateExport> newMultiStat(List<String> vinList, Date startDate, Date endDate) {
        List<LossRateExport> resultList = ListEx.newArrayList();
        List<Date> dates = getDateList(startDate, endDate);
        for (String vin : vinList) {
            for (Date date : dates) {
                LossRateExport export = new LossRateExport();
                LossRateResult result = newStat(vin, date);
                BeanUtils.copyProperties(result, export);
                export.setVin(vin);
                export.setDate(date);
                resultList.add(export);
            }
        }
        return resultList;
    }

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
}
