package com.zx.utils.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZhaoXu
 * @date 2021/11/12 13:20
 */
public class StringUtil {
    static Pattern numberPattern = Pattern.compile("[0-9]*");
    /**
     * time时间戳转Date
     *
     * @param time
     * @return
     */
    public static Date timeToDate(String time) {
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sdfTime.format(Long.valueOf(time));
        try {
            Date date = sdfTime.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期字符串转String型Time
     *
     * @param date
     * @return
     */
    public static String stringToTime(String date) {
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dateDate = sdfTime.parse(date);
            return String.valueOf(dateDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Matcher isNum = numberPattern.matcher(str);
        if (isEmpty(str) || !isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 首字母变小写
     * @param str
     * @return
     */
    public static String firstCharToLowerCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 首字母变大写
     * @param str
     * @return
     */
    public static String firstCharToUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 判断是否为空
     * @param str
     * @return
     */
    public static boolean isEmpty(final String str) {
        return (str == null) || (str.length() == 0);
    }
}
