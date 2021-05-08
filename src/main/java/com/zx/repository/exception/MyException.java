package com.zx.repository.exception;

/**
 * @author : zhaoxu
 */
public class MyException extends RuntimeException {
    private Integer code;

    private String message;

    public MyException(Integer code, String message) {
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
