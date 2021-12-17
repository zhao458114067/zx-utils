package com.zx.util.exception;

/**
 * @author : zhaoxu
 */
public class BaseException extends RuntimeException {
    private Integer code;

    private String message;

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}
