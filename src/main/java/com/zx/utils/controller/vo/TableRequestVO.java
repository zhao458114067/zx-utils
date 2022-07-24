package com.zx.utils.controller.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhaoxu
 */
@Data
public class TableRequestVO implements Serializable {
    private static final long serialVersionUID = -5401553539255852589L;
    /**
     * 当前页
     */
    private Integer currentPage;
    /**
     * 每页条数
     */
    private Integer pageSize;
    /**
     * jdbc-前缀
     */
    private String prepend;
    /**
     * jdbc-url
     */
    private String url;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 表名
     */
    private String tablename;
}
