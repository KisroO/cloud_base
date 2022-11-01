package com.kisro.cloud.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@Data
@Document(indexName = "test_custom", type = "_doc", shards = 2, replicas = 2)
public class Custom {
    @Id
    private Long id;
    @Field
    private String title;
    @Field
    private String content;
}
