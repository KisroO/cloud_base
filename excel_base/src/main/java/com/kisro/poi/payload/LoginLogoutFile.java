package com.kisro.poi.payload;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * @author zoutao
 * @since 2023/4/20
 **/
@Data
public class LoginLogoutFile extends BaseFile {
    @Excel(name = "ICCID")
    private String iccid;

}
