package com.zx.utils.controller.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhaoxu
 */
@Data
public class ColumnVO implements Serializable {
    private static final long serialVersionUID = 791649074826994226L;
    /**
     * 列表
     */
    private String columnName;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 备注
     */
    private String comments;
    /**
     * 驼峰属性
     */
    private String caseAttrName;
    /**
     * 普通属性
     */
    private String lowerAttrName;
    /**
     * 属性类型
     */
    private String attrType;
    /**
     * jdbc类型
     */
    private String jdbcType;
    /**
     * 其他信息
     */
    private String extra;

    /**
     * 是否可为空
     */
    private String nullAbled;
}
