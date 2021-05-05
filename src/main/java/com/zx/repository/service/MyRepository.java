package com.zx.repository.service;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author: zhaoxu
 * @description: JPA通用功能扩展
 */
@NoRepositoryBean
public interface MyRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * 分页条件查询
     *
     * @param tableMap    查询条件
     * @param excludeAttr 是字符串类型，但是不使用模糊查询的字段，可为空
     * @param joinField   外键关联查询，可为空
     * @param sortAttr    排序，可为空
     * @return
     */
    Page<T> findByPage(Map<String, String> tableMap, List<String> excludeAttr, Map joinField, String sortAttr);

    /**
     * 条件组合查询
     *
     * @param tableMap    查询条件
     * @param excludeAttr 是字符串类型，但是不使用模糊查询的字段，可为空
     * @param joinField   外键关联查询，可为空
     * @param sortAttr    排序，可为空
     * @return
     */
    List<T> findByConditions(Map<String, String> tableMap, List<String> excludeAttr, Map joinField, String sortAttr);

    /**
     * 假删
     *
     * @param ids ","隔开
     */
    void deleteValid(String ids) throws NoSuchFieldException;

    /**
     * 全匹配查询某一个实体，查询到多个只返回第一个
     *
     * @param attr      属性名称，一般是唯一标识（id、code ...）
     * @param condition 对应条件（1、罐1、TK1000 ...）
     * @return
     */
    T findOneByAttr(String attr, String condition);

    /**
     * 全匹配查询
     *
     * @param attr      属性名称（id、name、code ...）
     * @param condition 对应条件（1、罐1、TK1000 ...）
     * @return
     */
    List<T> findByAttr(String attr, String condition);

    /**
     * 全匹配查询实体
     *
     * @param attr       属性名称（id、code ...）
     * @param conditions 对应条件,逗号隔开
     * @return
     */
    List<T> findByAttrs(String attr, String conditions);
}
