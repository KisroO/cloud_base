package com.kisro.poi.payload;

import lombok.Data;

import java.util.Date;

/**
 * @author zoutao
 * @since 2023/4/24
 **/
@Data
public class OriginalMessage {
    private String vin;

    private String command;

    private Date acquisitionTime;

    private Date receiveTime;

    private String originalMessage;

    private String accFlag;
}
