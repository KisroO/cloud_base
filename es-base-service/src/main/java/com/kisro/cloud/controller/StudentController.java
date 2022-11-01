package com.kisro.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.kisro.cloud.pojo.Student;
import com.kisro.cloud.service.StudentService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kisro
 * @since 2022/11/1
 **/
@RestController
@RequestMapping("/search")
public class StudentController {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @PutMapping("/createIndex")
    public void createIndex() {
        elasticsearchRestTemplate.createIndex(Student.class);
        elasticsearchRestTemplate.putMapping(Student.class);
    }

    @DeleteMapping("/deleteIndex")
    public String deleteIndex() {
        elasticsearchRestTemplate.deleteIndex(Student.class);
        return "delete index success";
    }

    @PutMapping("/insertDoc")
    public String insertDocument(@RequestBody Student student) {
        studentService.insert(student);
        return "insert success";
    }

    @DeleteMapping("/deleteDoc/{id}")
    public String deleteDocument(@PathVariable("id") Long id) {
        studentService.delete(id);
        return "delete success";
    }

    @PutMapping("/updateDoc")
    public String updateDocument(@RequestBody Student student) {
        studentService.update(student);
        return "update success";
    }

    @GetMapping("/find/{id}")
    public String findById(@PathVariable("id") Long id) {
        Student student = studentService.findById(id);
        return JSONObject.toJSONString(student);
    }
}
