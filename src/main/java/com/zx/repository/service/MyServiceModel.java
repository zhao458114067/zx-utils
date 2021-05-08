package com.zx.repository.service;

import java.util.List;
import java.util.Map;

/**
 * service继承
 * @author : zhaoxu
 */
public interface MyServiceModel<T> {
    /**
     * 增
     *
     * @param entityVO 实体
     */
    void save(T entityVO);

    /**
     * 改
     *
     * @param entityVO 实体
     */
    void update(T entityVO);

    /**
     * 假删
     *
     * @param ids id,逗号隔开
     */
    void deleteValid(String ids);

    /**
     * 查某个实体
     *
     * @param attr 查询的属性
     * @param condition 条件
     * @return 实体
     */
    T findByAttr(String attr, String condition);

    /**
     * 分页查询
     *
     * @param tableMap 参数
     * @return Map
     */
    Map<String, Object> findByPage(Map<String, String> tableMap);

    /**
     * 条件查询
     *
     * @param tableMap 参数
     * @return list列表
     */
    List<T> findAllByConditions(Map<String, String> tableMap);
}
