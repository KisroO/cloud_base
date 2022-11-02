package com.kisro.cloud.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Kisro
 * @since 2022/11/2
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RowResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String rowKey;
    private String columnFamily;
    private String columnQualifier;
    private String rowValue;
}
