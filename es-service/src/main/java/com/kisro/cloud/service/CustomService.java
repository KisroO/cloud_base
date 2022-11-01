package com.kisro.cloud.service;

import com.kisro.cloud.pojo.Custom;

import java.util.List;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
public interface CustomService {
    void save(Custom custom);

    void delete(Long id);

    void update(Custom custom);

    Custom findById(Long id);

    List<Custom> findAll();
}
