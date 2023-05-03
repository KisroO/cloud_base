package com.kisro.poi.util;

import com.nex.bu1.bean.DoubleObjHolder;
import com.nex.bu1.util.DateEx;
import com.nex.bu1.util.ListEx;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hbase.types.RawInteger;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zoutao
 * @date 2023/4/27
 */
public class CommonUtils {
    /**
     * 校验 sourceTime 是否在 partDate当天
     * @param partDate
     * @param sourceTime
     * @return
     */
    public static boolean checkDatePartition(Date partDate,Long sourceTime){
        Date sourceDate = new Date();
        sourceDate.setTime(sourceTime);
        Pair<Date, Date> datePair = betweenDates(partDate);
        return checkDate(sourceDate,datePair.getLeft(),datePair.getRight());
    }

    public static boolean checkDate(Date target, Date begin, Date end) {
        long targetTime = target.getTime();
        return checkDate(targetTime,begin,end);
    }

    public static boolean checkDate(Long targetTime, Date begin, Date end) {
        return targetTime > begin.getTime() && targetTime < end.getTime();
    }

    /**
     * 获取开始和结束时间
     * @param date
     * @return
     */
    public static Pair<Date, Date> betweenDates(Date date) {
        Date begin = DateEx.beginOfDay(date);
        Date end = DateEx.endOfDay(date);
        return Pair.of(begin, end);
    }

    public static List<Date> getRecentSevenDate(Date currentDate){
        List<Date> dateList = ListEx.newArrayList();
        Date date = new Date(currentDate.getTime());
        for (int i = 0; i < 7; i++) {
            date = DateEx.plusDay(date,1);
            dateList.add(date);
        }
        return dateList;
    }

    /**
     * 获取日期列表
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<Date> betweenDateList(Date startDate, Date endDate) {
        LocalDate startBegin = DateEx.toLocalDate(DateEx.beginOfDay(startDate));
        LocalDate endBegin = DateEx.toLocalDate(DateEx.beginOfDay(endDate));
        long betweenDateNum = ChronoUnit.DAYS.between(startBegin, endBegin);
        return IntStream.iterate(0, i -> i + 1)
                .limit(betweenDateNum)
                .mapToObj(startBegin::plusDays)
                .map(DateEx::toDate)
                .collect(Collectors.toList());
    }
}
