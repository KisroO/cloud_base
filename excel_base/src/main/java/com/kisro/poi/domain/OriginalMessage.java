package com.kisro.poi.domain;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kisro.poi.enums.Command;
import com.nex.bu1.lang.ObjEx;
import com.nex.bu1.lang.StrEx;
import lombok.*;

import java.util.Date;

/**
 * @author zoutao
 * @since 2023/4/17
 **/
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class OriginalMessage {
    @JsonIgnore
    @Excel(name = "报文数据")
    private String hexMessage;

    @Excel(name = "命令标识")
    private String command;

    @Excel(name = "采集时间", format = "yyyy/MM/dd HH:mm:ss")
    private Date acquisitionTime;

    /**
     * 0或者负数都为异常
     *
     * @param pre
     * @return
     */
    public Long timeDiff(OriginalMessage pre) {
        if (pre == null) {
            return 2000L;
        }
        if (ObjEx.isAllNull(acquisitionTime, pre.getAcquisitionTime())) {
            return 2000L;
        }
        return acquisitionTime.getTime() - pre.getAcquisitionTime().getTime();
    }

    @JsonIgnore
    public boolean isLogin() {
        return StrEx.equals(Command.VEHICLE_LOGIN, command);
    }

    @JsonIgnore
    public boolean isLogout() {
        return StrEx.equals(Command.VEHICLE_LOGOUT, command);
    }

    @JsonIgnore
    public boolean isRealtimeData() {
        return StrEx.equals(Command.REALTIME_DATA, command);
    }

    @JsonIgnore
    public boolean isReuploadData() {
        return StrEx.equals(Command.REALTIME_REUPLOAD_DATA, command);
    }

}
