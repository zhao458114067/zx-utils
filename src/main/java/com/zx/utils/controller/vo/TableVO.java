package com.zx.utils.controller.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhaoxu
 */
@Data
public class TableVO implements Serializable {
    private static final long serialVersionUID = 7388697741407667586L;
    /**
     * 名称
     */
    private String tableName;
    /**
     * 备注
     */
    private String comments;
    /**
     * 主键
     */
    private ColumnVO pk;
    /**
     * 列名
     */
    private List<ColumnVO> columns;
    /**
     * 驼峰类型
     */
    private String caseClassName;
    /**
     * 普通类型
     */
    private String lowerClassName;
}
