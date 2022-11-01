package com.kisro.cloud.service;

import com.kisro.cloud.pojo.Report;

/**
 * @author Kisro
 * @since 2022/10/26
 **/
public interface IReportService {
    Report reportInfo(Long id);

//    void cacheReport(Report report);

    void deleteReport(Long id);

    long insert(Report report);

    void updateReport(Report report);
}
