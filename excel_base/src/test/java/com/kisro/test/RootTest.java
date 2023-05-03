package com.kisro.test;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.alibaba.fastjson.JSON;
import com.kisro.poi.payload.LossRateExport;
import com.kisro.poi.payload.LossRateResultExp;
import com.kisro.poi.util.CommonUtils;
import com.nex.bu1.bean.DoubleObjHolder;
import com.nex.bu1.json.JsonEx;
import com.nex.bu1.util.DateEx;
import com.nex.bu1.util.ListEx;
import net.minidev.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zoutao
 * @since 2023/4/12
 **/
public class RootTest {
    @Test
    public void test() {
        String a = "ss";
        String b = null;
        System.out.println(a.equals(b));
    }

    @Test
    public void test1() throws ParseException {
        String startDate = "2023/4/10 00:00:00";
        String endDate = "2023/4/17 00:00:00";
        Date d1 = DateEx.parse(startDate, DateEx.FMT_YMD_HMS3);
        Date d2 = DateEx.parse(endDate, DateEx.FMT_YMD_HMS3);
        List<Date> list = getDateList(d1, d2);
        System.out.println(JsonEx.toJsonString(list));
    }

    private List<Date> getDateList(Date startDate, Date endDate) {
        List<Date> dateList = ListEx.newArrayList();
        LocalDate startBegin = DateEx.toLocalDate(DateEx.beginOfDay(startDate));
        LocalDate endBegin = DateEx.toLocalDate(DateEx.beginOfDay(endDate));
//        Date endBegin = DateEx.beginOfDay(endDate);
        long betweenDateNum = ChronoUnit.DAYS.between(startBegin, endBegin);
        List<Date> list = IntStream.iterate(0, i -> i + 1)
                .limit(betweenDateNum)
                .mapToObj(startBegin::plusDays)
                .map(DateEx::toDate)
                .collect(Collectors.toList());
        return list;
    }

    @Test
    public void testFields() {
        List<Field> fieldList = ListEx.newArrayList();
        Class<?> clz = LossRateExport.class;
        List<ExcelExportEntity> entityList = ListEx.newArrayList();
        while (clz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clz.getDeclaredFields())));
            clz = clz.getSuperclass();
        }
        for (Field field : fieldList) {
            System.out.println(field.getName());
        }
    }


    public void splitByDay(Long start, Long end, List<DoubleObjHolder<Long, Long>> daySplit) {
        if (start < end) {
            daySplit.add(DoubleObjHolder.of(start, end));
        }
    }
    @Test
    public void testRate(){
        Long receivableCount = 5208L;
        Long receivedCount = 5100L;
        Long totalCount = 5223L;
        // 丢包率(规则一)
        BigDecimal receivableDecimal = new BigDecimal(receivableCount + "");
        BigDecimal receivedDecimal = new BigDecimal(receivedCount + "");
        BigDecimal percentageDecimal = new BigDecimal("100");
        BigDecimal rate1 = receivableDecimal.subtract(receivedDecimal)
                .divide(receivableDecimal, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(percentageDecimal);
        String rateStr = rate1.toPlainString();
        int length = rateStr.length();
        if(length >4){
            rateStr = rateStr.substring(0, length -2);
        }
        System.out.println(rate1);
        System.out.println(rateStr);
    }

    @Test
    public void testHexParse(){
        String hex = "F4";
        Integer res = Integer.valueOf(hex, 16);
        System.out.println( res);
    }

    @Test
    public void testDate(){
        List<Date> recentSevenDate = CommonUtils.getRecentSevenDate(DateEx.now());
        System.out.println(JsonEx.toJsonString(recentSevenDate));
    }

    @Test
    public void testNpe(){
        LossRateResultExp resultExp = new LossRateResultExp();
        List<LossRateExport> list = ListEx.newArrayList();
        list.add(resultExp.getResult());
        System.out.println(JSON.toJSONString(list));
    }
}
