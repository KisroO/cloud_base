package com.kisro.cloud.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * @author zoutao
 * @since 2023/3/9
 **/
@Data
public class Sim {
    @Excel(name = "sim")
    private String sim;

    @Excel(name = "chassis")
    private String chassis;
}
