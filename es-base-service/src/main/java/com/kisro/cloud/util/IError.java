package com.kisro.cloud.util;

/**
 * @author Kisro
 * @since 2022/11/7
 **/
public enum IError {
    NOT_FOUND("101", "not found");

    private String code;
    private String msg;

    private IError(String s, String s1) {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
