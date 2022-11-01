package com.kisro.cloud.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kisro.cloud.mapper.ReportMapper;
import com.kisro.cloud.pojo.Report;
import com.kisro.cloud.service.IReportService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    @Override
    public Report reportInfo(Long id) {
        return baseMapper.selectById(id);
    }


    @Override
    public void deleteReport(Long id) {
        baseMapper.deleteById(id);
    }

    @Override
    public long insert(Report report) {
        Assert.notNull(report, "插入数据不能为空");
        baseMapper.insert(report);
        return report.getId();
    }

    @Override
    public void updateReport(Report report) {
        // 校验...
        baseMapper.updateById(report);
    }


}
