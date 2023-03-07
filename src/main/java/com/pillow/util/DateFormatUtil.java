package com.pillow.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wujt
 * @ClassName DateFormatUtil
 * @Description 日格式化工具类
 * @date 2023/3/1
 */
public class DateFormatUtil {

    public static String getLocalDate(String pattern){
        LocalDate date = LocalDate.now();
        return getLocalDate(pattern,date);
    }

    public static String getLocalDate(String pattern,LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(date);
    }

    public static String getLocalDateTime(String pattern){
        LocalDateTime dateTime = LocalDateTime.now();
        return getLocalDateTime(pattern,dateTime);
    }

    public static String getLocalDateTime(String pattern,LocalDateTime dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(dateTime);
    }

    public static String getLocalTime(String pattern){
        LocalTime time = LocalTime.now();
        return getLocalTime(pattern,time);
    }

    public static String getLocalTime(String pattern, LocalTime localTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(localTime);
    }
}
