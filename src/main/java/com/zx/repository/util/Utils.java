package com.zx.repository.util;

import com.alibaba.fastjson.JSONObject;
import com.zx.repository.constant.Constants;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
 * @author zhaoxu
 */
@Component
public class Utils {

    static Pattern numberPattern = Pattern.compile("[0-9]*");

    public static void main(String[] args) throws IOException {

    }

    /**
     * 获取当前格式化时间
     */
    public Date getNowDate() {
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
    public String getStringFromInputStream(InputStream inputStream) throws IOException {
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
    public String getIp() {
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
    public Date timeToDate(String time) {
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
    public String stringToTime(String date) {
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
    public Sort sortAttr(Map<String, String> tableMap, String sorterBy) {
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
    public boolean isNumeric(String str) {
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
    public boolean isMergedRegion(Sheet sheet, int row, int column) {
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
    public String getMergedRegionValue(Sheet sheet, int row, int column) {
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

    /**
     * 按行读取全部文件数据
     *
     * @param strFile
     */
    public StringBuffer readFile(String strFile) throws IOException {
        StringBuffer strSb = new StringBuffer();
        InputStreamReader inStrR = new InputStreamReader(new FileInputStream(strFile), "UTF-8");
        // character streams
        BufferedReader br = new BufferedReader(inStrR);
        String line = br.readLine();
        while (line != null) {
            strSb.append(line).append("\r\n");
            line = br.readLine();
        }
        return strSb;
    }

    /**
     * 写入文件
     *
     * @param fileName
     * @param s
     * @throws IOException
     */
    public void writeToFile(String fileName, String s) throws IOException {
        File f1 = new File(fileName);
        OutputStream out = null;
        BufferedWriter bw = null;
        if (f1.exists()) {
            out = new FileOutputStream(f1);
            bw = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
            bw.write(s);
            bw.flush();
            bw.close();
        } else {
            System.out.println("文件不存在");
        }
    }

    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr()的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值
     *
     * @return ip
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        System.out.println("x-forwarded-for ip: " + ip);
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            System.out.println("Proxy-Client-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            System.out.println("WL-Proxy-Client-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            System.out.println("HTTP_CLIENT_IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            System.out.println("HTTP_X_FORWARDED_FOR ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
            System.out.println("X-Real-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            System.out.println("getRemoteAddr ip: " + ip);
        }
        System.out.println("获取客户端ip: " + ip);
        return ip;
    }

    /**
     * 获取
     *
     * @param objectClass
     * @param annoClass
     * @return
     */
    public List<Field> getTargetAnnoation(Class<?> objectClass, Class<? extends Annotation> annoClass) {
        List<Field> fields = new ArrayList<>();
        Field[] declaredFields = objectClass.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            //是否拥有指定注解类，如果没有就返回null， 有的话则返回这个注解类对象
            if (!field.isAnnotationPresent(annoClass)) {
                continue;
            } else {
                fields.add(field);
            }
        }
        if (!CollectionUtils.isEmpty(fields)) {
            return fields;
        } else {
            return null;
        }
    }

    /**
     * 转换字符串
     *
     * @param str
     * @param tClass
     * @return
     */
    public Object covertStr(String str, Class<?> tClass) {
        if (tClass == Long.class) {
            return Long.valueOf(str);
        } else if (tClass == Integer.class || tClass == int.class || tClass == short.class) {
            return Integer.valueOf(str);
        } else {
            return str;
        }
    }
}
