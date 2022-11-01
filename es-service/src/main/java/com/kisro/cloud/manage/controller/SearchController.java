package com.kisro.cloud.manage.controller;

import com.alibaba.fastjson.JSONArray;
import com.kisro.cloud.manage.service.CustomService;
import com.kisro.cloud.pojo.Custom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private CustomService customService;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @GetMapping("/createIndex")
    public void createIndex(String indexName) {
        elasticsearchTemplate.createIndex(indexName);
    }

    @PostMapping("/insert")
    public String insertReport(@RequestBody Custom custom) {
        customService.save(custom);
        return "insert success";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteReport(@PathVariable("id") Long id) {
        customService.delete(id);
        return "delete success";
    }

    @GetMapping("/find/{id}")
    public String findReport(@PathVariable("id") Long id) {
        return customService.findById(id).toString();
    }

    @PutMapping("/update")
    public String update(@RequestBody Custom custom) {
        customService.update(custom);
        return "update success";
    }

    @GetMapping("/findAll")
    public String findAll() {
        List<Custom> list = customService.findAll();
        return JSONArray.toJSONString(list);
    }

}
