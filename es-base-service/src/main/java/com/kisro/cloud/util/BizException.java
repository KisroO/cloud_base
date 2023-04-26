package com.kisro.cloud.util;

/**
 * @author Kisro
 * @since 2022/11/7
 **/
public class BizException extends RuntimeException {
    private IError iError;

    public BizException(IError error) {
        super(error.getCode() + ":" + error.getMsg());
        this.iError = error;
    }
}
