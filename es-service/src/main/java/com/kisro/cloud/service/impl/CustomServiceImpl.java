package com.kisro.cloud.service.impl;

import com.kisro.cloud.dao.ReportRepository;
import com.kisro.cloud.pojo.Custom;
import com.kisro.cloud.service.CustomService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@Service
@AllArgsConstructor
@NoArgsConstructor
public class CustomServiceImpl implements CustomService {
    private ReportRepository reportRepository;

    @Override
    public void save(Custom custom) {
        reportRepository.save(custom);
    }

    @Override
    public void delete(Long id) {
        reportRepository.deleteById(id);
    }

    @Override
    public void update(Custom custom) {
        reportRepository.save(custom);
    }

    @Override
    public Custom findById(Long id) {
        return reportRepository.findById(id).get();
    }

    @Override
    public List<Custom> findAll() {
        Iterable<Custom> all = reportRepository.findAll();
        List<Custom> list = new ArrayList<>();
        all.forEach(list::add);
        return list;
    }


}
