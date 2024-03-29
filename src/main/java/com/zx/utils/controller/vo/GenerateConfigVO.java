package com.zx.utils.controller.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : zhaoxu
 */
@Data
public class GenerateConfigVO implements Serializable {
    private static final long serialVersionUID = 6085993053823321781L;
    /**
     * 请求参数
     */
    private TableRequestVO request;
    /**
     * 包名
     */
    private String packageName;
    /**
     * 作者
     */
    private String author;
    /**
     * 模块名称
     */
    private String moduleName;
    /**
     * 表前缀
     */
    private String tablePrefix;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 表备注
     */
    private String comments;

    /**
     * 路径名字
     */
    private String mainPath;
}

