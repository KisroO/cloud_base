package com.kisro.poi.service;

import com.chinaway.columnar.exception.ColumnarClientException;
import com.chinaway.columnar.hbase.HBaseColumnarClient;
import com.kisro.poi.enums.Command;
import com.kisro.poi.payload.AccOriginMsg;
import com.kisro.poi.payload.OriginalMessage;
import com.kisro.poi.util.CommonUtils;
import com.kisro.poi.util.EsUtil;
import com.ne.ice.icsp.access.core.utils.ByteUtil;
import com.nex.bu1.bean.DoubleObjHolder;
import com.nex.bu1.lang.StrEx;
import com.nex.bu1.util.ListEx;
import com.nxe.galaxy.dynamic.commons.entity.OriginalMessageRecord;
import com.nxe.galaxy.fv.protocol.datacell.FvgbBatchLocationReportDataCell;
import com.nxe.galaxy.fv.protocol.datacell.FvgbBatchLocationReportDataItemCell;
import com.nxe.galaxy.fv.protocol.datacell.FvgbLocationReportDataCell;
import com.nxe.galaxy.fv.protocol.datacell.embedded.location.BaseLocation;
import com.nxe.galaxy.fv.protocol.dflq.DflqGBPayload;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zoutao
 * @since 2023/4/24
 **/
@Service
@RequiredArgsConstructor
public class HBaseService {
    public static final Long ONE_DAY = 24 * 60 * 60 * 1000L;
    private final HBaseColumnarClient hbaseClient;
    private static final Map<Integer, String> COMMAND_MAP = new HashMap<>();

    static {
        COMMAND_MAP.put(0x01, "车辆登入");
        COMMAND_MAP.put(0x02, "实时信息上报");
        COMMAND_MAP.put(0x03, "补发信息上报");
        COMMAND_MAP.put(0x04, "车辆登出");
        COMMAND_MAP.put(0xE8, "位置信息汇报");
        COMMAND_MAP.put(0xF4, "位置信息盲区批量上传");
    }

    /**
     * 获取指定日期的所有原始报文数据
     *
     * @param vin
     * @param startDate
     * @param endDate
     * @param commandList
     * @return
     */
    public List<OriginalMessageRecord> findOriginalList(String vin, Date startDate, Date endDate, List<Integer> commandList) {
        List<OriginalMessageRecord> resultList = new ArrayList<>();
        List<Pair<Long, Long>> timeSplit = new ArrayList<>();
        splitByDay(startDate.getTime(), endDate.getTime(), timeSplit);
        for (Pair<Long, Long> pair : timeSplit) {
            for (Integer command : commandList) {
                String startRow = EsUtil.createOriginalRowKey(vin, command, pair.getRight());
                String endRow = EsUtil.createOriginalRowKey(vin, command, pair.getLeft());
                try {
                    List<OriginalMessageRecord> sourceList = hbaseClient.findObjectList(StrEx.getBytes(startRow), StrEx.getBytes(endRow), OriginalMessageRecord.class);
                    resultList.addAll(sourceList);
                } catch (ColumnarClientException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultList;
    }

    /**
     * 获取下一天的补发信息与位置盲区信息
     *
     * @param vin
     * @param startDate   开始时间
     * @param endDate     结束时间
     * @param commandList 命令
     * @param targetDate  查询补发数据的日期
     * @return left: 目标日期补发与盲区报文数据  right: 是否查询下一天,true:是,false:否
     */
    public Pair<List<OriginalMessageRecord>, Boolean> findNextReUploadData(String vin, Date startDate, Date endDate, List<Integer> commandList, Date targetDate) {
        List<OriginalMessageRecord> resultList = new ArrayList<>();
        Pair<Long, Long> pair = Pair.of(startDate.getTime(), endDate.getTime());
        // 结束搜索标志 v1,v2都为true表示两个命令都已查找结束，无需进行下次查询
        DoubleObjHolder<Boolean, Boolean> endFlagPair = DoubleObjHolder.of(false, false);
        for (Integer command : commandList) {
            String startRow = EsUtil.createOriginalRowKey(vin, command, pair.getRight());
            String endRow = EsUtil.createOriginalRowKey(vin, command, pair.getLeft());
            List<OriginalMessageRecord> sourceList = null;
            try {
                // 获取补发与位置盲区数据后排序
                sourceList = hbaseClient.findObjectList(StrEx.getBytes(startRow), StrEx.getBytes(endRow), OriginalMessageRecord.class)
                        .stream()
                        .sorted(Comparator.comparing(OriginalMessageRecord::getAcquisitionTime))
                        .collect(Collectors.toList());
            } catch (ColumnarClientException e) {
                e.printStackTrace();
            }
            if (CollectionUtils.isEmpty(sourceList)) {
                continue;
            }
            // 计算是否需要继续查找补发与位置盲区数据
            for (OriginalMessageRecord record : sourceList) {
                Long acquisitionTime = record.getAcquisitionTime();
                if (CommonUtils.checkDatePartition(targetDate, acquisitionTime)) {
                    // 补发或位置盲区数据在目标日期中则加入集合
                    resultList.add(record);
                } else {
                    // 补发和位置盲区都无目标日期数据，返回数据列表并设置下次查询标志为false
                    if (endFlagPair.v1 && endFlagPair.v2) {
                        return Pair.of(resultList, false);
                    }
                    if (command == 0x03) {
                        endFlagPair.setV1(true);
                    } else {
                        endFlagPair.setV2(true);
                    }
                }
            }
        }
        return Pair.of(resultList, true);
    }

    public List<AccOriginMsg> processOriginalMessage(List<OriginalMessageRecord> messageRecordList,String vin){
        List<AccOriginMsg> resList = ListEx.newArrayList();
        for (OriginalMessageRecord record : messageRecordList) {
            String commandHexStr = record.getOriginalMessage().substring(4, 6);
            Integer commandInt = Integer.valueOf(commandHexStr, 16);
            String commandStr = COMMAND_MAP.get(commandInt);
            // 无acc状态数据处理
            if(Command.NO_ACC_COMMAND_LIST.contains(commandStr)){
                resList.add(normalHandler(vin, commandStr, record));
            } else if (commandInt == 0xE8){
                // 位置信息处理
                resList.add(locationProcess(record,vin));
            } else if(commandInt == 0xF4){
                // 位置盲区信息数据
                resList.addAll(locationReUploadProcess(record,vin));
            }
        }
        return resList;
    }

    /**
     * 获取处理后的原始报文数据(用于统计部分)
     * 第一版
     * @param vin
     * @param startDate
     * @param endDate
     * @param commandList
     * @return
     */
    public List<AccOriginMsg> findList(String vin, Date startDate, Date endDate, List<Integer> commandList) {
        List<AccOriginMsg> resultList = new ArrayList<>();
        List<Pair<Long, Long>> timeSplit = new ArrayList<>();
        splitByDay(startDate.getTime(), endDate.getTime(), timeSplit);
        for (Pair<Long, Long> pair : timeSplit) {
            for (Integer command : commandList) {
                String startRow = EsUtil.createOriginalRowKey(vin, command, pair.getRight());
                String endRow = EsUtil.createOriginalRowKey(vin, command, pair.getLeft());
                String commandStr = COMMAND_MAP.get(command);
                List<AccOriginMsg> dataList = null;
                try {
                    List<OriginalMessageRecord> sourceList = hbaseClient.findObjectList(StrEx.getBytes(startRow), StrEx.getBytes(endRow), OriginalMessageRecord.class);
                    // 位置数据处理
                    if (command == 0xF4 || command == 0xE8) {
                        dataList = locationHandler(vin, sourceList);
                    } else {
                        // 实时或登入登出数据处理
                        dataList = normalHandlerList(vin, commandStr, sourceList);
                    }
                    resultList.addAll(dataList);
                } catch (ColumnarClientException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultList;
    }

    /**
     * 实时或登入登出数据处理
     *
     * @param vin
     * @param command
     * @param sourceList
     * @return
     */
    private List<AccOriginMsg> normalHandlerList(String vin, String command, List<OriginalMessageRecord> sourceList) {
        List<AccOriginMsg> dataList = sourceList
                .parallelStream()
                .map(item -> {
//                    OriginalMessage msg = new OriginalMessage();
//                    msg.setVin(vin);
//                    // 原始报文(可选)
////                                msg.setOriginalMessage(item.getOriginalMessage());
//                    Date acqDate = new Date();
//                    acqDate.setTime(item.getAcquisitionTime());
//                    Date receivedDate = new Date();
//                    receivedDate.setTime(item.getUploadTime());
//                    msg.setAcquisitionTime(acqDate);
//                    msg.setReceiveTime(receivedDate);
//                    msg.setCommand(command);
                    return normalHandler(vin, command, item);
                })
//                .sorted(Comparator.comparing(OriginalMessage::getAcquisitionTime))
                .collect(Collectors.toList());
        return dataList;
    }

    /**
     * 登入等处、实时与补发数据处理
     * @param vin
     * @param command
     * @param record
     * @return
     */
    public AccOriginMsg normalHandler(String vin,String command,OriginalMessageRecord record){
        AccOriginMsg msg = new AccOriginMsg();
        msg.setVin(vin);
        Date acqDate = new Date();
        acqDate.setTime(record.getAcquisitionTime());
        Date receivedDate = new Date();
        receivedDate.setTime(record.getUploadTime());
        msg.setAcquisitionTime(acqDate);
        msg.setReceiveTime(receivedDate);
        msg.setCommand(command);
        return msg;
    }

    /**
     * 位置数据处理
     *
     * @param vin
     * @param sourceList
     * @return
     */
    private List<AccOriginMsg> locationHandler(String vin, List<OriginalMessageRecord> sourceList) {
        List<AccOriginMsg> resultList = ListEx.newArrayList();
        for (OriginalMessageRecord record : sourceList) {
            String commandHexStr = record.getOriginalMessage().substring(4, 6);
            Integer commandInt = Integer.valueOf(commandHexStr, 16);
            if(commandInt == 0xE8){
                // 位置信息
                resultList.add(locationProcess(record, vin));
            } else {
                // 位置盲区信息上报
                resultList.addAll(locationReUploadProcess(record, vin));
            }
        }
        return resultList;
        // 位置数据
//        List<AccOriginMsg> locationList = sourceList.stream()
//                .filter(item -> {
//                    String commandStr = item.getOriginalMessage().substring(4, 6);
//                    return "E8".equals(commandStr);
//                })
//                .map(item -> {
//                    String commandStr = item.getOriginalMessage().substring(4, 6);
//                    AccOriginMsg msg = new AccOriginMsg();
//                    String originalMessage = item.getOriginalMessage();
////                    msg.setOriginalMessage(originalMessage);
//                    String statusHexStr = originalMessage.substring(56, 64);
//                    msg.setAccFlag(getAccStatus(statusHexStr));
//                    msg.setVin(vin);
//                    Long uploadTime = item.getUploadTime();
//                    Date receivedDate = new Date();
//                    receivedDate.setTime(uploadTime);
//                    msg.setReceiveTime(receivedDate);
//
//                    Long acqTime = item.getAcquisitionTime();
//                    Date acqDate = new Date();
//                    acqDate.setTime(acqTime);
//                    msg.setAcquisitionTime(receivedDate);
//                    String command = "E8".equals(commandStr) ? "位置信息汇报" : "位置信息盲区批量上传";
//                    msg.setCommand(command);
////                    return msg;
//                    return locationProcess(item,vin);
//                })
//                .collect(Collectors.toList());
//
//        sourceList.stream()
//                .filter(item -> {
//                    String commandStr = item.getOriginalMessage().substring(4, 6);
//                    return !"E8".equals(commandStr);
//                })
//                .forEach(item -> {
//                    String originalMessage = item.getOriginalMessage();
//                    // 接收时间
//                    Long uploadTime = item.getUploadTime();
//                    Date receivedDate = new Date();
//                    receivedDate.setTime(uploadTime);
//                    List<AccOriginMsg> msgList = getAccStatusByMsg(originalMessage, receivedDate, vin);
//                    locationList.addAll(msgList);
//                });
//        return locationList;
    }

    /**
     * 位置信息处理
     * @param record
     * @return
     */
    private AccOriginMsg locationProcess(OriginalMessageRecord record,String vin){
        AccOriginMsg msg = new AccOriginMsg();
        String originalMessage = record.getOriginalMessage();
//                    msg.setOriginalMessage(originalMessage);
        String statusHexStr = originalMessage.substring(56, 64);
        msg.setAccFlag(getAccStatus(statusHexStr));
        msg.setVin(vin);
        Long uploadTime = record.getUploadTime();
        Date receivedDate = new Date();
        receivedDate.setTime(uploadTime);
        msg.setReceiveTime(receivedDate);

        Long acqTime = record.getAcquisitionTime();
        Date acqDate = new Date();
        acqDate.setTime(acqTime);
        msg.setAcquisitionTime(receivedDate);
        msg.setCommand(COMMAND_MAP.get(0xE8));
        return msg;
    }

    /**
     * 位置盲区信息数据处理
     * @param record
     * @param vin
     * @return
     */
    public List<AccOriginMsg> locationReUploadProcess(OriginalMessageRecord record,String vin){
        String originalMessage = record.getOriginalMessage();
        // 接收时间
        Long uploadTime = record.getUploadTime();
        Date receivedDate = new Date();
        receivedDate.setTime(uploadTime);
        return getAccStatusByMsg(originalMessage, receivedDate, vin);
    }

    private List<AccOriginMsg> getAccStatusByMsg(String originalMessage, Date uploadDate, String vin) {
        List<AccOriginMsg> msgList = ListEx.newArrayList();
        DflqGBPayload<FvgbBatchLocationReportDataCell> payload = null;
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(ByteUtil.hexStringToBytes(originalMessage.replaceAll(" ", "")));
            payload = new DflqGBPayload<>();
            payload.decode(byteBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FvgbBatchLocationReportDataCell body = payload.getBody();
        List<FvgbBatchLocationReportDataItemCell> dataCells = body.getFvgbLocationReportDataCells();
        for (FvgbBatchLocationReportDataItemCell dataCell : dataCells) {
            FvgbLocationReportDataCell cell = dataCell.getCell();
            BaseLocation baseLocation = cell.getBaseLocation();
            // 采集时间
            long acqTime = baseLocation.getTime().toMilliseconds();
            Date acqDate = new Date();
            acqDate.setTime(acqTime);
            // ACC 状态
            int statusFlag = baseLocation.getStatusFlag();
            String accStatus = transformAccStatus(statusFlag);

            AccOriginMsg msg = new AccOriginMsg();
            msg.setVin(vin);
//            msg.setOriginalMessage(originalMessage);
            msg.setAccFlag(accStatus);
            msg.setAcquisitionTime(acqDate);
            msg.setReceiveTime(uploadDate);
            msg.setCommand(payload.getCommand().getName());
            msgList.add(msg);
        }
        return msgList;
    }

    /**
     * 根据原始报文，获取状态字段对应的ACC 状态
     *
     * @param statusHexStr
     * @return 开 or 关
     */
    private String getAccStatus(String statusHexStr) {
        byte[] bytes = ByteUtil.hexStringToBytes(statusHexStr);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int status = buffer.getInt();
        return transformAccStatus(status);
    }

    private String transformAccStatus(int status) {
        String binStr = Integer.toBinaryString(status);
        String accStatus = binStr.substring(binStr.length() - 1);
        return "0".equals(accStatus) ? "关" : "开";
    }

    private void splitByDay(Long start, Long end, List<Pair<Long, Long>> timeSplit) {
        if (start < end) {
            if (end - start > ONE_DAY) {
                Long renewEnd = end - ONE_DAY;
                timeSplit.add(Pair.of(renewEnd, end));
                //System.out.println("add startTime:" + DateEx.format(DateEx.from(renewEnd)) + "  endTime:" + DateEx.format(DateEx.from(end)));
                splitByDay(start, renewEnd, timeSplit);
            } else {
                timeSplit.add(Pair.of(start, end));
            }
        }

    }
}
