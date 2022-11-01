package com.kisro.cloud.client;

import com.kisro.cloud.manage.config.HBaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@Component
@DependsOn("HBaseConfig")
@Slf4j
public class HBaseClient {
    @Resource
    private HBaseConfig config;

    private static Connection connection = null;
    private static Admin admin = null;

    @PostConstruct
    public void init() {
        if (connection != null) {
            return;
        }
        try {
            connection = ConnectionFactory.createConnection(config.configuration());
            admin = connection.getAdmin();
        } catch (IOException e) {
            log.error("create hbase connection failed : {}", e);
        }
    }

    /**
     * create table
     *
     * @param tableName      表明
     * @param columnFamilies 列簇名
     */
    public void createTable(String tableName, String... columnFamilies) {
        TableName name = TableName.valueOf(tableName);
        TableDescriptorBuilder descriptorBuilder = TableDescriptorBuilder.newBuilder(name);
        List<ColumnFamilyDescriptor> list = new ArrayList<>();
        for (String columnFamily : columnFamilies) {
            ColumnFamilyDescriptor columnFamilyDescriptor = ColumnFamilyDescriptorBuilder.newBuilder(columnFamily.getBytes()).build();
            list.add(columnFamilyDescriptor);
        }
        TableDescriptor tableDescriptor = descriptorBuilder.setColumnFamilies(list).build();
        try {
            admin.createTable(tableDescriptor);
        } catch (IOException e) {
            log.error("create table failed : ", e);
        }
    }

    public void insertOrUpdate(String tableName, String rowKey, String columnFamily, String column, String value) {
        this.insertOrUpdate(tableName, rowKey, columnFamily, new String[]{column}, new String[]{value});
    }

    public void insertOrUpdate(String tableName, String rowKey, String columnFamily, String[] columns, String[] values) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            byte[] columnFamilyBytes = columnFamily.getBytes();
            for (int i = 0; i < columns.length; i++) {
                put.addColumn(columnFamilyBytes, Bytes.toBytes(columns[i]), Bytes.toBytes(values[i]));
                table.put(put);
            }
        } catch (IOException e) {
            log.error("insert or update data failed : ", e);
        }
    }

    public String getValue(String tableName, String rowKey, String family, String column) {
        Table table = null;
        String value = "";
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(family)
                || StringUtils.isBlank(rowKey) || StringUtils.isBlank(column)) {
            return null;
        }
        try {
            table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(rowKey.getBytes());
            get.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            Result result = table.get(get);
            List<Cell> cells = result.listCells();
            if (cells != null && cells.size() > 0) {
                for (Cell cell : cells) {
                    value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                }
            }
        } catch (IOException e) {
            log.error("get value failed : {}", e);
        } finally {
            try {
                table.close();
                connection.close();
            } catch (IOException e) {
                log.error("get value close failed : {}", e);
            }
        }
        return value;
    }

    public void deleteColumn(String tableName, String rowKey, String columnFamily, String column) {
        try (Table table = connection.getTable(TableName.valueOf(tableName));) {
            Delete delete = new Delete(rowKey.getBytes());
            delete.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column));
            table.delete(delete);
        } catch (IOException e) {
            log.info("delete column failed : {}", e);
        }

    }

    public void deleteTable(String tableName) {
        try {
            TableName name = TableName.valueOf(tableName);
            admin.disableTable(name);
            admin.deleteTable(name);
        } catch (IOException e) {
            log.error("delete table failed : {}", e);
        }
    }

    public void deleteRow(String tableName, String rowKey) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Delete delete = new Delete(rowKey.getBytes());
            table.delete(delete);
        } catch (IOException e) {
            log.error("delete row error, row key: {}, cause: {}", rowKey, e);
        }

    }
}
