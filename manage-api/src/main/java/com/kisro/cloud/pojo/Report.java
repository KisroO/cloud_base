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
 * @since 2022/10/26
 **/
@TableName("report")
@Data
@ToString
@ApiModel
@AllArgsConstructor
@NoArgsConstructor
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "报表id")
    private Long id;

    public Report(String title, String uuid, String content) {
        this.title = title;
        this.uuid = uuid;
        this.content = content;
    }

    @TableField
    @ApiModelProperty(value = "标题")
    private String title;

    @TableField
    @ApiModelProperty("uuid")
    private String uuid;

    @TableField
    @ApiModelProperty(value = "内容")
    private String content;
}
