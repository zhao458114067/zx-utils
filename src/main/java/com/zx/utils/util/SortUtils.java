package com.zx.utils.util;

import com.alibaba.fastjson.JSONObject;
import com.zx.utils.constant.Constants;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.Map;

/**
 * @author zhaoxu
 */
public class SortUtils {
    /**
     * 表格排序
     *
     * @param tableMap tableMap
     * @param sorterBy 默认按此属性排序
     * @return Sort
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
}
