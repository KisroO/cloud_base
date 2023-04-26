package com.kisro.poi.domain;

import com.chinaway.columnar.annotation.Column;
import com.chinaway.columnar.annotation.RowKey;
import com.chinaway.columnar.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zoutao
 * @since 2023/4/24
 **/
@Table(
        name = "original_message_record"
)
@Data
public class OriginalMessageRecord implements Serializable {
    private static final long serialVersionUID = -7606210038542394292L;
    @RowKey
    private String id;
    @Column(
            family = "f",
            name = "energy_type"
    )
    private String energyType;
    @Column(
            family = "f",
            name = "upload_time"
    )
    private Long uploadTime;
    @Column(
            family = "f",
            name = "acquisition_time"
    )
    private Long acquisitionTime;
    @Column(
            family = "f",
            name = "original_message"
    )
    private String originalMessage;
}
