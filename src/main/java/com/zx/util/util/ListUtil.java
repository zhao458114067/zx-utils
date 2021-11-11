package com.zx.util.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: zhaoxu
 * @description:
 */
public class ListUtil {
    /**
     * 列表转为字符串
     *
     * @param data  数据
     * @param split 分隔符
     * @return  字符串
     */
    public static String joinList(List data, String split) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.size(); i++) {
            Object item = data.get(i);
            //跳过null值
            if (item != null) {
                result.append(item.toString());
                //如果不是最后一个
                if (i + 1 < data.size()) {
                    result.append(split);
                }
            }
        }
        return result.toString();
    }

    /**
     * 字符串转为list
     *
     * @param data  数据
     * @param split 切割符号
     * @return  列表
     */
    public static List<String> splitIntoList(String data, String split) {
        //为空
        if (data == null) {
            return null;
        }
        String[] splitArray = data.split(split);
        if (splitArray.length > 0) {
            List<String> resultList = new ArrayList<>();
            for (String item : splitArray) {
                //自动跳过空值
                if (item != null && !"".equals(item)) {
                    resultList.add(item);
                }
            }
            return resultList;
        } else {
            return null;
        }
    }

    /**
     * 分页
     *
     * @param data  数据
     * @param current  页数
     * @param pageSize 每页条数
     * @return  map
     */
    public static <E> Map<String, Object> toPage(List<E> data, int current, int pageSize) {
        //判空
        if (data == null || data.size() <= 0) {
            return null;
        }
        Map<String, Object> destMap = new HashMap<>(4);
        List<E> pageList = data.stream()
                .skip((current - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        //分页需要的参数
        destMap.put("list", pageList);
        destMap.put("current", current);
        destMap.put("pageSize", pageSize);
        destMap.put("total", data.size());
        return destMap;
    }

    /**
     * 查找列表1在列表2中的差异项
     * @param list1 列表1
     * @param list2 列表2
     * @param <E>   泛型
     * @return  差异列表
     */
    public static <E> List<E> getListDiff(List<E> list1, List<E> list2) {
        if (list1 == null || list1.isEmpty()) {
            return list2 == null || list2.isEmpty() ? new ArrayList<>() : list2;
        }

        if (list2 == null || list2.isEmpty()) {
            return list1 == null || list1.isEmpty() ? new ArrayList<>() : list1;
        }

        Set<E> diffSet = new HashSet<>(list1);
        Set<E> setOfCommonElements = new HashSet<>(list2);

        //先把列表2全部加进来
        diffSet.addAll(list2);
        //调用方法保留共有元素
        setOfCommonElements.retainAll(list1);

        //移除共有的
        diffSet.removeAll(setOfCommonElements);

        return new ArrayList<>(diffSet);
    }
}

