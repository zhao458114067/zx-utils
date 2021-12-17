package com.zx.util.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author ZhaoXu
 * @date 2021/11/12 14:24
 */
public class MapUtil {
    @FunctionalInterface
    /**
     * 内部接口
     */
    public interface Comparetor<T> {
        /**
         * 比较函数
         *
         * @param o1
         * @param o2
         * @return
         */
        Boolean compare(T o1, T o2);
    }

    /**
     * 过滤map
     *
     * @param map
     * @param predicate
     * @return
     */
    public static <T> Map<String, T> filter(Map<String, T> map, Predicate<Map.Entry<String, T>> predicate) {
        Map<String, T> newMap = new HashMap<>(8);
        for (Map.Entry<String, T> entry : map.entrySet()) {
            //按照自定义规则过滤
            String key = entry.getKey();
            T value = entry.getValue();
            //调用test方法
            if (predicate.test(entry)) {
                newMap.put(key, value);
            }
        }
        return newMap;
    }

    /**
     * map排序
     *
     * @param map
     * @param comparator lambda自定义比较条件
     * @param <T>
     * @return
     */
    public static <T> List<Map.Entry<String, T>> sort(Map<String, T> map, Comparetor<Map.Entry<String, T>> comparator) {
        //先存到列表,好做处理
        List<Map.Entry<String, T>> resultList = new ArrayList<>();
        resultList.addAll(map.entrySet());
        quickSort(resultList, 0, map.size() - 1, comparator);
        return resultList;
    }

    /**
     * 快排
     *
     * @param a
     * @param start
     * @param end
     */
    public static <L> void quickSort(List<L> a, int start, int end, Comparetor<L> comparator) {
        if (a.size() < 0) {
            return;
        }
        if (start >= end) {
            return;
        }
        int left = start;
        int right = end;
        L temp = a.get(left);
        while (left < right) {
            //从右面找
            while (left < right && comparator.compare(a.get(right), temp)) {
                right--;
            }
            //从左面找
            a.set(left, a.get(right));
            while (left < right && !comparator.compare(a.get(left), temp)) {
                left++;
            }
            //把坑填上
            a.set(right, a.get(left));
        }
        a.set(left, temp);
        quickSort(a, start, left - 1, comparator);
        quickSort(a, left + 1, end, comparator);
    }

    /**
     * 按照正序排序
     *
     * @param entry1
     * @param entry2
     * @param <T>
     * @return
     */
    public static <T> Boolean ascByValue(Map.Entry<String, T> entry1, Map.Entry<String, T> entry2) {
        return entry1.getValue().toString().compareTo(entry2.getValue().toString()) >= 0;
    }

    /**
     * 按照倒序排序
     *
     * @param entry1
     * @param entry2
     * @param <T>
     * @return
     */
    public static <T> Boolean descByValue(Map.Entry<String, T> entry1, Map.Entry<String, T> entry2) {
        return entry1.getValue().toString().compareTo(entry2.getValue().toString()) <= 0;
    }

    /**
     * 按照正序排序
     *
     * @param entry1
     * @param entry2
     * @param <T>
     * @return
     */
    public static <T> Boolean ascByKey(Map.Entry<String, T> entry1, Map.Entry<String, T> entry2) {
        return entry1.getKey().compareTo(entry2.getKey()) >= 0;
    }

    /**
     * 按照倒序排序
     *
     * @param entry1
     * @param entry2
     * @param <T>
     * @return
     */
    public static <T> Boolean descByKey(Map.Entry<String, T> entry1, Map.Entry<String, T> entry2) {
        return entry1.getKey().compareTo(entry2.getKey()) <= 0;
    }

    /**
     * 判断map为空
     *
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Boolean isEmpty(Map<K, V> map) {
        return map == null || map.size() == 0;
    }
}

