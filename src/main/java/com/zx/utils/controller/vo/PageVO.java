package com.zx.utils.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author ZhaoXu
 * @date 2022/7/24 1:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<S> implements Serializable {
    private static final long serialVersionUID = -3355752076145642645L;
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
