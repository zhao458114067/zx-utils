package com.zx.repository.service;

import java.util.List;
import java.util.Map;

/**
 * @author: zhaoxu
 * @description:
 */
public interface MyServiceModel<T> {
    /**
     * 增
     *
     * @param entityVO
     */
    void save(T entityVO);

    /**
     * 改
     *
     * @param entityVO
     */
    void update(T entityVO);

    /**
     * 假删
     *
     * @param ids
     */
    void deleteValid(String ids);

    /**
     * 查某个实体
     *
     * @param attr
     * @param condition
     * @return
     */
    T findByAttr(String attr, String condition);

    /**
     * 分页查询
     *
     * @param tableMap
     * @return
     */
    Map<String, Object> findByPage(Map<String, String> tableMap);

    /**
     * 条件查询
     *
     * @param tableMap
     * @return
     */
    List<T> findAllByConditions(Map<String, String> tableMap);

    /**
     * 外键关联
     *
     * @return
     */
    Map createJoinField();
}
