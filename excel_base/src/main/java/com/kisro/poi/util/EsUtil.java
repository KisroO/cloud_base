package com.kisro.poi.util;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zoutao
 * @since 2023/4/24
 **/
public class EsUtil {
    public final static Long MAX_21_CENTURY_MILLIS = 4102415999999L;

    /**
     * 将任意对象转换为SolrInputDocument
     *
     * @param target
     * @param <T>
     * @return
     */
    public static <T> Map convert(T target) {
        Map document = new HashMap(0);
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            ReflectionUtils.makeAccessible(field);
            document.put(field.getName(), ReflectionUtils.getField(field, target));
        }
        return document;
    }

    /**
     * 所有原始数据hbase的rowkey
     * vin反转+1+command（三位整数长度，不足补零）+MAX_21_CENTURY_MILLIS-时间
     */
    public static String createOriginalRowKey(String vin, int commandId, Long time) {
        return new StringBuilder(vin).reverse().toString() +
                "1" + String.format("%3d", commandId).replace(" ", "0")
                + (MAX_21_CENTURY_MILLIS - time);
    }

    /**
     * 解析后的数据hbase的rowkey
     * vin反转+MAX_21_CENTURY_MILLIS-时间
     */
    public static String createParsedDataRowKey(String vin, Long time) {
        return new StringBuilder(vin).reverse().toString() + (MAX_21_CENTURY_MILLIS - time);
    }
}
