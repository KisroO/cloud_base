package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * @author zoutao
 * @since 2023/4/20
 **/
@Data
public class AccOriginMsg {
    @Excel(name = "vin")
    private String vin;

    @Excel(name = "命令标识")
    private String command;

    @Excel(name = "终端采集时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date acquisitionTime;

    @Excel(name = "平台接收时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;

    @Excel(name = "ACC状态")
    private String accFlag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof AccOriginMsg)) return false;

        AccOriginMsg that = (AccOriginMsg) o;

        return new EqualsBuilder()
                .append(getVin(), that.getVin())
                .append(getCommand(), that.getCommand())
                .append(getAcquisitionTime(), that.getAcquisitionTime())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getVin())
                .append(getCommand())
                .append(getAcquisitionTime())
                .toHashCode();
    }
}
