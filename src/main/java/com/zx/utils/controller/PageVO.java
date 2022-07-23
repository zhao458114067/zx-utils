package com.zx.utils.controller;

import lombok.Data;

import java.util.List;

/**
 * @author ZhaoXu
 * @date 2022/7/24 1:58
 */
@Data
public class PageVO<S> {
    /**
     * 总数
     */
    Long total;

    /**
     * 当前页
     */
    Integer current;

    /**
     * 每页条数
     */
    Integer pageSize;

    /**
     * 数据
     */
    List<S> data;
}
