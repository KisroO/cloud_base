package com.kisro.cloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kisro.cloud.pojo.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    @Select("select count(distinct ) from vehicle v left join report r on v.uuuid = r.uuid")
    Long count();

    void insertData(Report report);
}
