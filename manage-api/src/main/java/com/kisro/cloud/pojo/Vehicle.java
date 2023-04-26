package com.kisro.cloud.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Kisro
 * @since 2022/12/23
 **/
@TableName("vehicle")
@Data
@ToString
@ApiModel
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "报表id")
    private Long id;

    @TableField
    @ApiModelProperty("uuid")
    private String uuid;

    @TableField
    @ApiModelProperty("chassis_umber")
    private String chassisNumber;

    public Vehicle(String uuid, String chassisNumber) {
        this.uuid = uuid;
        this.chassisNumber = chassisNumber;
    }
}
