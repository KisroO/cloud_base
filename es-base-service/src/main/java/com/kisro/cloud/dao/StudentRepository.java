package com.kisro.cloud.dao;

import com.kisro.cloud.pojo.Student;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kisro
 * @since 2022/11/1
 **/
@Repository
public interface StudentRepository extends ElasticsearchRepository<Student, Long> {
}
