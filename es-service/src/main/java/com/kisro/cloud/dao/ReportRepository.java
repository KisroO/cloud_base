package com.kisro.cloud.dao;

import com.kisro.cloud.pojo.Custom;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@Repository
public interface ReportRepository extends ElasticsearchRepository<Custom, Long> {
}
