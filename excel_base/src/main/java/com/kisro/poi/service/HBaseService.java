package com.kisro.poi.service;

import com.chinaway.columnar.exception.ColumnarClientException;
import com.chinaway.columnar.hbase.HBaseColumnarClient;
import com.kisro.poi.payload.OriginalMessage;
import com.kisro.poi.util.EsUtil;
import com.ne.ice.icsp.access.core.utils.ByteUtil;
import com.nex.bu1.lang.StrEx;
import com.nex.bu1.util.ListEx;
import com.nxe.galaxy.dynamic.commons.entity.OriginalMessageRecord;
import com.nxe.galaxy.fv.protocol.datacell.FvgbBatchLocationReportDataCell;
import com.nxe.galaxy.fv.protocol.datacell.FvgbBatchLocationReportDataItemCell;
import com.nxe.galaxy.fv.protocol.datacell.FvgbLocationReportDataCell;
import com.nxe.galaxy.fv.protocol.datacell.embedded.location.BaseLocation;
import com.nxe.galaxy.fv.protocol.dflq.DflqGBPayload;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;
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

    public List<OriginalMessage> findList(String vin, Date startDate, Date endDate, List<Integer> commandList) {
        List<OriginalMessage> resultList = new ArrayList<>();
        List<Pair<Long, Long>> timeSplit = new ArrayList<>();
        splitByDay(startDate.getTime(), endDate.getTime(), timeSplit);
        for (Pair<Long, Long> pair : timeSplit) {
            for (Integer command : commandList) {
                String startRow = EsUtil.createOriginalRowKey(vin, command, pair.getRight());
                String endRow = EsUtil.createOriginalRowKey(vin, command, pair.getLeft());
                String commandStr = COMMAND_MAP.get(command);
                List<OriginalMessage> dataList = null;
                try {
                    List<OriginalMessageRecord> sourceList = hbaseClient.findObjectList(StrEx.getBytes(startRow), StrEx.getBytes(endRow), OriginalMessageRecord.class);
                    // 位置数据处理
                    if (command == 0xF4 || command == 0xE8) {
                        dataList = locationHandler(vin, sourceList);
                    } else {
                        // 实时或登入登出数据处理
                        dataList = normalHandler(vin, commandStr, sourceList);
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
    private List<OriginalMessage> normalHandler(String vin, String command, List<OriginalMessageRecord> sourceList) {
        List<OriginalMessage> dataList = sourceList
                .parallelStream()
                .map(item -> {
                    OriginalMessage msg = new OriginalMessage();
                    msg.setVin(vin);
                    // 原始报文(可选)
//                                msg.setOriginalMessage(item.getOriginalMessage());
                    Date acqDate = new Date();
                    acqDate.setTime(item.getAcquisitionTime());
                    Date receivedDate = new Date();
                    receivedDate.setTime(item.getUploadTime());
                    msg.setAcquisitionTime(acqDate);
                    msg.setReceiveTime(receivedDate);
                    msg.setCommand(command);
                    return msg;
                })
//                .sorted(Comparator.comparing(OriginalMessage::getAcquisitionTime))
                .collect(Collectors.toList());
        return dataList;
    }

    /**
     * 位置数据处理
     *
     * @param vin
     * @param sourceList
     * @return
     */
    private List<OriginalMessage> locationHandler(String vin, List<OriginalMessageRecord> sourceList) {
        // 位置数据
        List<OriginalMessage> locationList = sourceList.stream()
                .filter(item -> {
                    String commandStr = item.getOriginalMessage().substring(4, 6);
                    return "E8".equals(commandStr);
                })
                .map(item -> {
                    String commandStr = item.getOriginalMessage().substring(4, 6);
                    OriginalMessage msg = new OriginalMessage();
                    String originalMessage = item.getOriginalMessage();
                    msg.setOriginalMessage(originalMessage);
                    String statusHexStr = originalMessage.substring(56, 64);
                    msg.setAccFlag(getAccStatus(statusHexStr));
                    msg.setVin(vin);
                    Long uploadTime = item.getUploadTime();
                    Date receivedDate = new Date();
                    receivedDate.setTime(uploadTime);
                    msg.setReceiveTime(receivedDate);

                    Long acqTime = item.getAcquisitionTime();
                    Date acqDate = new Date();
                    acqDate.setTime(acqTime);
                    msg.setAcquisitionTime(receivedDate);
                    String command = "E8".equals(commandStr) ? "位置信息汇报" : "位置信息盲区批量上传";
                    msg.setCommand(command);
                    return msg;
                })
                .collect(Collectors.toList());

        sourceList.stream()
                .filter(item -> {
                    String commandStr = item.getOriginalMessage().substring(4, 6);
                    return !"E8".equals(commandStr);
                })
                .forEach(item -> {
                    String originalMessage = item.getOriginalMessage();
                    // 接收时间
                    Long uploadTime = item.getUploadTime();
                    Date receivedDate = new Date();
                    receivedDate.setTime(uploadTime);
                    List<OriginalMessage> msgList = getAccStatusByMsg(originalMessage, receivedDate, vin);
                    locationList.addAll(msgList);
                });
        return locationList;
    }

    private List<OriginalMessage> getAccStatusByMsg(String originalMessage, Date uploadDate, String vin) {
        List<OriginalMessage> msgList = ListEx.newArrayList();
        ByteBuffer byteBuffer = ByteBuffer.wrap(ByteUtil.hexStringToBytes(originalMessage.replaceAll(" ", "")));
        DflqGBPayload<FvgbBatchLocationReportDataCell> payload = new DflqGBPayload<>();
        payload.decode(byteBuffer);
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

            OriginalMessage msg = new OriginalMessage();
            msg.setVin(vin);
            msg.setOriginalMessage(originalMessage);
            msg.setAccFlag(accStatus);
            msg.setAcquisitionTime(acqDate);
            msg.setReceiveTime(uploadDate);
            msg.setCommand(COMMAND_MAP.get(0xF4));
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
