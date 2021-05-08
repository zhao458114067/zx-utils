package com.zx.repository.constant;

/**
 * 错误码
 * @author : zhaoxu
 */
public enum ErrorCodeEnum {
    // 1000, "参数错误"
    PARAMS_ERROR(1000, "参数错误"),

    // 1001, "未找到属性类型"
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

    public String getErrorMessage() {
        return errorMessage;
    }
}
