package com.zx.utils.util;

import cn.hutool.db.Entity;
import com.zx.utils.controller.vo.TableRequestVO;
import com.zx.utils.service.impl.GenerateCodeServiceImpl;

import java.sql.SQLException;
import java.util.List;

/**
 * @author : zhaoxu
 */
public class GenerateCodeTest {
    static String userName = "postgres";
    static String password = "123456";
    static String url = "localhost:5432/test";
    static String prepend = "jdbc:postgresql://";
    static String tableName = "";
    static String auther = "zhaoxu";
    static String moduleName = "testmodule";
    static String packageName = "com.supcon.mare.tankinfo";
    static String mainPath = "oms";

    public static void main(String[] args) throws SQLException, NoSuchFieldException, IllegalAccessException {

        //设置请求参数
        TableRequestVO tableRequestVO = new TableRequestVO();
        tableRequestVO.setUsername(userName);
        tableRequestVO.setUrl(url);
        tableRequestVO.setPrepend(prepend);
        tableRequestVO.setPassword(password);
        tableRequestVO.setTablename(tableName);
        tableRequestVO.setCurrentPage(1);
        DynamicObject dynamicVO = DynamicObject.parseMap(new GenerateCodeServiceImpl().queryTable(tableRequestVO));
        //查询对应数据库下所有的表
        List<Entity> tables = dynamicVO.getToObject("tables" );
        tables.forEach(item -> {
            try {
                //生成所有表
                CodeGenerateUtil.startAutoGenerateCode(prepend, url, item.getStr("tableName" ),
                        userName, password, auther, moduleName, packageName, mainPath);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }
}
