//package com.kisro.cloud.controller;
//
//import com.kisro.cloud.client.HBaseClient;
//import lombok.AllArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
///**
// * @author Kisro
// * @since 2022/10/28
// **/
//@RestController
//@RequestMapping("/hbase")
//@AllArgsConstructor
//public class HbaseController {
//    private static final String TABLE_NAME = "test_record";
//    private static final String DEV_TABLE_NAME = "access:command_send_record";
//    private static final String FAM_1 = "quick";
//    private static final String FAM_2 = "normal";
//    private static final String DEV_FAM = "fs";
//    private HBaseClient client;
//
//    @PostMapping("/createTable")
//    public String createTable() {
//        client.createTable(TABLE_NAME, FAM_1, FAM_2);
//        return "table has created";
//    }
//
//    @DeleteMapping("/deleteTable")
//    public String deleteTable() {
//        client.deleteTable(TABLE_NAME);
//        return "table has deleted";
//    }
//
//    @PostMapping("/insert")
//    public String insert() {
//        client.insertOrUpdate(TABLE_NAME, "1", FAM_1, "speed", "1km/h");
//        client.insertOrUpdate(TABLE_NAME, "1", FAM_1, "fell", "better");
//        client.insertOrUpdate(TABLE_NAME, "1", FAM_2, "action", "create table");
//        client.insertOrUpdate(TABLE_NAME, "1", FAM_2, "time", "2020/10/28");
//        client.insertOrUpdate(TABLE_NAME, "1", FAM_2, "user", "admin");
//        return "insert success";
//    }
//
//    @GetMapping("/find")
//    public String findOne() {
//        return client.getValue(TABLE_NAME, "1", FAM_1, "speed");
//    }
//
//    @DeleteMapping("/deleteRow")
//    public String deleteRow() {
//        client.deleteRow(TABLE_NAME, "1");
//        return "delete row success";
//    }
//
//    @DeleteMapping("/deleteColumn")
//    public String deleteColumn() {
//        client.deleteColumn(TABLE_NAME, "1", FAM_2, "action");
//        return "column has deleted";
//    }
//
//    @GetMapping("/selectOneRow")
//    public String selectOneRow() {
//        return client.selectOneRow(TABLE_NAME, "1");
//    }
//
//}
