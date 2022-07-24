package com.zx.utils.service;

import cn.hutool.db.Entity;
import com.zx.utils.controller.vo.GenerateConfigVO;
import com.zx.utils.controller.vo.PageVO;
import com.zx.utils.controller.vo.TableRequestVO;

import java.sql.SQLException;

/**
 * @author zhaoxu
 */
public interface GenerateCodeService {
    /**
     * 生成代码
     *
     * @param generateConfigVO 生成配置
     * @throws SQLException sql错误
     * @return 代码压缩文件
     */
    byte[] generatorCode(GenerateConfigVO generateConfigVO) throws SQLException;

    /**
     * 分页查询表信息
     *
     * @param request 请求参数
     * @return 表名分页信息
     */
    PageVO<Entity> listTables(TableRequestVO request);
}
