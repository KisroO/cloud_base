package com.kisro.cloud.service;

import com.chinaway.columnar.exception.ColumnarClientException;
import com.chinaway.columnar.hbase.HBaseColumnarClient;
import com.kisro.cloud.pojo.OriginalMessageRecordExp;
import com.kisro.cloud.util.EsUtil;
import com.nex.bu1.lang.StrEx;
import com.nex.bu1.util.DateEx;
import com.nex.bu1.util.ListEx;
import com.nxe.galaxy.dynamic.commons.entity.OriginalMessageRecord;
import com.nxe.galaxy.fv.protocol.FvgbCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zoutao
 * @since 2023/3/29
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {
    private final HBaseColumnarClient hbaseClient;

    public List<OriginalMessageRecordExp> queryOriginal(String vin, String commandIds, Long startTime, Long endTime) {
        List<OriginalMessageRecordExp> rst = ListEx.newLinkedList();

        FvgbCommand[] commands;
        if (StrEx.isBlank(commandIds)) {
            commands = FvgbCommand.values();
        } else {
            String[] split = StrEx.split(commandIds, ",");
            commands = new FvgbCommand[split.length];
            for (int i = 0; i < split.length; i++) {
                byte[] bytes = StrEx.hexStrToBytes(split[i]);
                commands[i] = FvgbCommand.valueOf(bytes[0]);
            }
        }
        /* 导出时，时间差最大为一天 */
        startTime = Math.max(startTime, endTime - (24 * 60 * 60 * 1000L * 7));
        log.debug("export time range: {} - {}", startTime, endTime, DateEx.format(DateEx.from(startTime)), DateEx.format(DateEx.from(endTime)));

        for (FvgbCommand value : commands) {
            int commandId = Byte.toUnsignedInt(value.getId());
            String startRow = EsUtil.createOriginalRowKey(vin, commandId, endTime);
            String endRow = EsUtil.createOriginalRowKey(vin, commandId, startTime);
            try {
                List<OriginalMessageRecord> resultList = hbaseClient.findObjectList(StrEx.getBytes(startRow), StrEx.getBytes(endRow), OriginalMessageRecord.class);
                List<OriginalMessageRecordExp> data =
                        resultList
                                .parallelStream()
                                .map(e -> {
                                    OriginalMessageRecordExp ex = new OriginalMessageRecordExp();
                                    ex.setCommand(value.getName());
                                    long time = e.getAcquisitionTime() <= 0 ? e.getUploadTime() : e.getAcquisitionTime();
                                    ex.setTime(DateEx.format(DateEx.from(time)));
                                    ex.setOriginalMessage(StrEx.replace(e.getOriginalMessage(), " ", ""));
                                    setJson(ex);
                                    return ex;
                                })
                                .sorted(Comparator.comparing(OriginalMessageRecordExp::getTime))
                                .collect(Collectors.toList());
                rst.addAll(data);
            } catch (ColumnarClientException e) {
                log.warn("", e);
            }
        }
        return rst;
    }

    private void setJson(OriginalMessageRecordExp ex) {
        try {
            ByteBuffer bf = ByteBuffer.wrap(StrEx.hexStrToBytes(ex.getOriginalMessage()));
            bf.position(4);
            byte[] vinBytes = new byte[17];
            bf.get(vinBytes);
            ex.setVin(StrEx.newString(vinBytes));
        } catch (Exception e) {
            log.warn("parse hex message failed.", e);
        }
    }
}
