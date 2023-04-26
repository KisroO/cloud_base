package com.kisro.cloud.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Kisro
 * @since 2022/11/7
 **/
@Data
@TableName("vehicle")
public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    @TableField(value = "chassis_number")
    private String chassisNumber;
}
