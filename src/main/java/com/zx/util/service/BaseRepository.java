package com.zx.util.service;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * JPA通用功能扩展
 *
 * @author : zhaoxu
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    /**
     * 分页条件查询
     *
     * @param objConditions   查询条件
     * @param excludeLikeAttr 是字符串类型，但是不使用模糊查询的字段，可为空
     * @param sortAttr        排序，可为空
     * @return Page
     */
    Page<T> findByPage(Map<String, String> objConditions, Integer current, Integer pageSize, List<String> excludeLikeAttr, String sortAttr);

    /**
     * 条件组合查询，省去不必要的关联map参数
     *
     * @param objConditions   查询条件
     * @param excludeLikeAttr 是字符串类型，但是不使用模糊查询的字段，可为空
     * @param sortAttr        排序，可为空
     * @return list列表
     */
    List<T> findByConditions(Map<String, String> objConditions, List<String> excludeLikeAttr, String sortAttr);

    /**
     * 假删
     *
     * @param ids ","隔开
     */
    void deleteValid(String ids);

    /**
     * 全匹配查询某一个实体，查询到多个只返回第一个
     *
     * @param attr      属性名称，唯一标识（id、code ...）
     * @param condition 对应条件（1、TK1000 ...）
     * @return 实体
     */
    T findOneByAttr(String attr, String condition);

    /**
     * 全匹配查询
     *
     * @param attr      属性名称（id、name、code ...）
     * @param condition 对应条件（1、罐1、TK1000 ...）
     * @return list列表
     */
    List<T> findByAttr(String attr, String condition);
}