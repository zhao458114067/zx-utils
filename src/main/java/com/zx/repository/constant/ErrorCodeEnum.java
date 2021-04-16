package com.zx.repository.constant;

/***
 * @author: zhaoxu
 * @description:
 */
public enum ErrorCodeEnum {
    // 3401, "参数错误"
    PARAMS_ERROR(1000, "参数错误"),

    // 3418, "未找到属性类型"
    CANNOT_FIND_ATTR_ERROR(1001, "未找到属性类型");

    private Integer errorCode;
    private String errorMessage;

    ErrorCodeEnum(Integer errorCode, String erroeMessage) {
        this.errorCode = errorCode;
        this.errorMessage = erroeMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
