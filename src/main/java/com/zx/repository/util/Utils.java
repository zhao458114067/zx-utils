package com.zx.repository.util;

import com.alibaba.fastjson.JSONObject;
import com.zx.repository.constant.Constants;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zss
 */
public class Utils {

    static Pattern numberPattern = Pattern.compile("[0-9]*");

    public static void main(String[] args) throws IOException {

    }

    /**
     * 类型转换
     */
    public static List<Long> getTypeConvert(List<String> ids) {
        List<Long> list = new ArrayList<>();
        if (null != ids && ids.size() > 0) {
            for (String id : ids) {
                list.add(Long.valueOf(id != null ? id : "0"));
            }
        } else {
            list.add(0L);
        }

        return list;
    }

    /**
     * 获取当前格式化时间
     */
    public static Date getNowDate() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
        String dateStr = dateFormat.format(now);
        try {
            Date parse = dateFormat.parse(dateStr);
            return parse;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return now;
    }

    /**
     * 从输入流读取完整字符串
     */
    public static String getStringFromInputStream(InputStream inputStream) throws IOException {
        String s = "";
        StringBuffer sb = new StringBuffer();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bufferedReader.close();
            inputStream.close();
        }
        return sb.toString();
    }

    /**
     * 获取当前ip
     *
     * @return
     * @throws UnknownHostException
     */
    public static String getIp() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }

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
     * 表格排序
     *
     * @param tableMap
     * @param sorterBy 默认按此属性排序
     * @return
     */
    public static Sort sortAttr(Map<String, String> tableMap, String sorterBy) {
        Sort sort;
        if (tableMap.get(Constants.SORTER) != null && !Constants.EMPTY_SORTER.equals(tableMap.get(Constants.SORTER))) {
            JSONObject sorter = JSONObject.parseObject(tableMap.get(Constants.SORTER));
            Iterator<String> iterator = sorter.keySet().iterator();
            String sortAttr = iterator.next();
            if (Constants.ASCEND.equals(sorter.get(sortAttr))) {
                sort = new Sort(Sort.Direction.ASC, sortAttr);
            } else {
                sort = new Sort(Sort.Direction.DESC, sortAttr);
            }
        } else {
            sort = new Sort(Sort.Direction.ASC, sorterBy);
        }
        return sort;
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Matcher isNum = numberPattern.matcher(str);
        if (StringUtils.isEmpty(str) || !isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 判断指定的单元格是否是合并单元格
     *
     * @param sheet
     * @param row    行下标
     * @param column 列下标
     * @return
     */
    public static boolean isMergedRegion(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取合并单元格的值
     *
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    public static String getMergedRegionValue(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    Row fRow = sheet.getRow(firstRow);
                    Cell fCell = fRow.getCell(firstColumn);
                    fCell.setCellType(CellType.STRING);
                    return fCell.getStringCellValue();
                }
            }
        }
        return null;
    }
}
